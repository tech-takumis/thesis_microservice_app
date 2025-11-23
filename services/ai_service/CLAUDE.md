# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a **Python FastAPI microservice** for rice disease prediction using machine learning. It's part of a larger microservices architecture and integrates with:
- **PostgreSQL** database for storing prediction results
- **Kafka** for consuming application submission events
- **MinIO** object storage for image management
- **TensorFlow/Keras** for AI model inference

The service automatically processes rice leaf images when PCIC applications are submitted via Kafka, performs disease classification, calculates severity, and stores results.

## Development Commands

### Running the Service
```bash
# Install dependencies
pip install -r requirements.txt

# Run with auto-reload (development)
uvicorn main:app --reload

# Run in production mode
uvicorn main:app --host 0.0.0.0 --port 8000
```

### Database
The database tables are automatically created on startup via SQLAlchemy's `Base.metadata.create_all(bind=engine)` in `main.py`. No manual migration commands needed.

Database connection is configured in `config/database.py`:
- URL: `postgresql+psycopg2://agripro:agripro@localhost:5432/ai_service`
- Uses SQLAlchemy ORM with session management via `get_db()` dependency

## Architecture

### Layer Structure (Controller → Service → Model)

```
controller/ai_controller.py
    ↓ (calls)
service/ai_service.py  ← service/prediction_service.py
    ↓ (uses)
models/ai_result.py
```

1. **Controllers** (`controller/ai_controller.py`)
   - FastAPI route handlers under `/ai` prefix
   - Handle HTTP requests, validation, and response formatting
   - Use dependency injection for database sessions via `Depends(get_db)`

2. **Services** (two distinct services)
   - `service/ai_service.py`: Business logic for database operations, MinIO interactions, and orchestrating predictions
   - `service/prediction_service.py`: ML model inference and image processing pipeline
   - Static methods in `AIService` for all business operations

3. **Models** (`models/ai_result.py`)
   - SQLAlchemy ORM models: `AIResult`, `Top3Prediction`, `LeafAnalysisImage`
   - Pydantic DTOs: `AIRequestDTO`, `AIResponseDTO`, `Top3PredictionDTO`, `LeafAnalysisImageDTO`
   - Database has unique constraint on `(applicationId, userId)` pair

### Kafka Consumer Architecture

The service runs a **background daemon thread** that automatically processes Kafka events:

- Started on FastAPI startup via `@app.on_event("startup")` hook in `main.py`
- Consumer runs in separate thread (see `service/kafka_consume_service.py`)
- Listens to `application-submitted` topic (configurable via env var)
- Automatically processes all messages (no filtering by provider in current implementation)
- For each event:
  1. Extracts `objectKeysForAIAnalysis` (list of MinIO object paths)
  2. Downloads each image from MinIO (tries multiple bucket names)
  3. Runs AI prediction via `prediction_service.RICEMASTER_PERFECTION()`
  4. Selects the result with highest confidence
  5. Saves to database with top-3 predictions and analysis images
  6. Falls back to "Unknown" prediction if any step fails

**Important**: Database session management in Kafka consumer uses manual session creation/cleanup pattern (not FastAPI dependency injection).

### Prediction Pipeline (RICEMASTER_PERFECTION)

Located in `service/prediction_service.py`, the prediction pipeline performs:

1. **Image Enhancement**: CLAHE (Contrast Limited Adaptive Histogram Equalization) in LAB color space
2. **Leaf Segmentation**: HSV color detection + GrabCut algorithm
3. **Lesion Detection**: Adaptive thresholding + brown color detection in HSV
4. **Superpixel Refinement**: SLIC segmentation to refine lesion boundaries
5. **Severity Calculation**: `lesion_ratio = (lesion_area / leaf_area) * 100`
6. **Model Prediction**: EfficientNet-based model inference
7. **Confidence Adjustment**: Custom algorithm in `adjust_confidence_score()` that boosts raw predictions
8. **Analysis Image Generation**: Creates isolated leaf and lesion overlay images, uploads to MinIO

**Model Details**:
- Path: `ai_model/rice_master_model.keras`
- Input: Automatically resized based on `model.input_shape[1:3]`
- Preprocessing: `tf.keras.applications.efficientnet.preprocess_input()`
- Classes: 12 rice diseases (see `class_names` in `PredictionService.__init__()`)

### MinIO Integration

Two separate buckets are used:
- **Documents bucket** (`DOCUMENT_BUCKET`): Source images from application submissions
- **AI service bucket** (`ai-service-bucket`): Uploaded predictions and analysis images

The service tries multiple bucket names when downloading (see `_download_image_from_minio()` in `service/ai_service.py`) to handle different naming conventions.

All uploaded images get presigned URLs with 2-hour expiration.

### Database Schema

Three tables with relationships:

```
ai_result (main table)
  ├── top3_prediction (many-to-one, cascade delete)
  └── leaf_analysis_image (many-to-one, cascade delete)
```

Key fields in `AIResult`:
- `applicationId`, `userId`: Links to external application system (unique together)
- `result`: Predicted disease class name
- `prediction`: Formatted prediction text
- `accuracy`: Confidence as string percentage (e.g., "95.67%")
- `confidence`: Float confidence value (0-100)
- `severity`: Lesion ratio percentage (0-100)
- `lesion_area`, `leaf_area`: Pixel counts from image analysis
- `image_path`: MinIO object key for original uploaded image

`Top3Prediction` stores the top 3 most confident predictions with rank.

`LeafAnalysisImage` stores metadata for generated analysis images (isolated leaf, lesion overlay).

## Configuration

All configuration uses environment variables with defaults:

**Database** (`config/database.py`):
- `DATABASE_URL` (hardcoded default)

**Kafka** (`config/kafka_config.py`):
- `KAFKA_BOOTSTRAP_SERVERS` (default: `localhost:29092`)
- `KAFKA_GROUP_ID` (default: `ai-service-group`)
- `KAFKA_TOPIC` (default: `application-submitted`)

**MinIO** (`config/minio_config.py`):
- `MINIO_ENDPOINT` (default: `localhost:9000`)
- `MINIO_ACCESS_KEY` (default: `minio`)
- `MINIO_SECRET_KEY` (default: `minio123`)
- `MINIO_SECURE` (default: `false`)
- `MINIO_BUCKET` (default: `ai-service-bucket`)
- `DOCUMENT_BUCKET` (default: `documents`)

## Key Implementation Patterns

### Error Handling in Kafka Consumer

The Kafka consumer has extensive error handling with fallback mechanism:
- Individual image failures don't stop batch processing
- Database rollback on errors
- Multiple attempts to create fallback "Unknown" results
- Comprehensive logging at each step

### Confidence Score Adjustment

The `adjust_confidence_score()` method artificially boosts raw model predictions to ensure results appear more confident. This is intentional product behavior, not a bug.

### Database Session Management

Two patterns used:
1. **FastAPI routes**: Use `db: Session = Depends(get_db)` dependency injection
2. **Kafka consumer**: Manual session creation via `db = next(get_db())` with explicit `try/finally` cleanup

### Analysis Image Storage

When predictions are made, two additional images are generated and uploaded to MinIO:
- **isolated_leaf**: Segmented leaf region (RGB)
- **lesion_overlay**: Original image with colored lesion overlay (using COLORMAP_JET)

These are stored in the `analysis/` prefix with unique identifiers and returned with presigned URLs.

## API Endpoints

All endpoints under `/ai` prefix:

- `GET /ai/`: Get all AI results
- `POST /ai/`: Manually add AI result (rarely used)
- `GET /ai/{result_id}`: Get specific result by ID
- `POST /ai/predict`: Upload image for prediction (form data: file, application_id, user_id)
- `GET /ai/statistics`: Get prediction statistics (optional query param: application_id)
- `GET /ai/{result_id}/image`: Get presigned URL for original uploaded image
- `GET /ai/{result_id}/analysis-images`: Get isolated leaf and lesion overlay images
- `GET /ai/applications/{application_id}`: Get results by application ID

Response format (all endpoints return):
```json
{
  "success": true/false,
  "data": {...},
  "message": "..."
}
```

## Important Notes

- The model file `ai_model/rice_master_model.keras` must exist for the service to start
- Kafka consumer runs automatically on startup and cannot be stopped without restarting the service
- All database operations use transaction management with commit/rollback
- MinIO buckets are auto-created if they don't exist
- Temporary files are created during prediction and cleaned up automatically
- The service logs to both console and `ai_service.log` file
