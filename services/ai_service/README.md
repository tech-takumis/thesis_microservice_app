# AI Service - Rice Disease Prediction Implementation

## Overview

This AI service provides rice disease prediction capabilities using a trained Keras model. It includes image processing, disease classification, and severity estimation features.

## Features

✅ **Rice Disease Classification**: Uses the `rice_master_model.keras` model to classify rice diseases  
✅ **Severity Estimation**: Calculates lesion ratio to estimate disease severity  
✅ **Image Processing**: Advanced image preprocessing with CLAHE enhancement and GrabCut segmentation  
✅ **RESTful API**: FastAPI endpoints for disease prediction  
✅ **Database Integration**: Saves prediction results to PostgreSQL  
✅ **Kafka Integration**: Consumes application events and processes PCIC submissions  
✅ **MinIO Object Storage**: Secure image storage with presigned URL access  

## API Endpoints

### 1. Get All AI Results
```
GET /ai/
Response: List[AIResponseDTO]
```

### 2. Get Specific AI Result
```
GET /ai/{result_id}
Response: AIResponseDTO
```

### 3. Add AI Result
```
POST /ai/
Body: AIRequestDTO
Response: AIResponseDTO
```

### 4. Predict Disease from Upload
```
POST /ai/predict
Form Data:
  - file: UploadFile (image)
  - application_id: str
Response: {
  "success": true,
  "data": {
    "id": int,
    "predicted_class": str,
    "confidence": float,
    "severity": float,
    "application_id": str
  }
}
```

### 5. Predict Disease from Path (Testing)
```
POST /ai/predict-from-path
Form Data:
  - image_path: str
  - application_id: str
Response: Same as predict endpoint
```

### 6. Get Image URL
```
GET /ai/{result_id}/image
Response: {
  "success": true,
  "image_url": str,
  "expires_in": int
}
```

## Model Classes

The model is trained to classify the following rice diseases:
- **Healthy**: No disease detected
- **Brown Spot**: Fungal disease causing brown spots
- **Hispa**: Pest damage from rice hispa beetles
- **Leaf Blast**: Fungal disease causing lesions

## Database Schema

```sql
CREATE TABLE ai_result (
    id SERIAL PRIMARY KEY,
    applicationId VARCHAR,
    result VARCHAR,        -- Disease class name
    prediction VARCHAR,    -- Formatted prediction text
    accuracy VARCHAR,      -- Confidence percentage
    image_path VARCHAR     -- MinIO object path
);
```

## Dependencies

Core packages required:
```
fastapi[standard]
sqlalchemy
psycopg2-binary
tensorflow
opencv-python
scikit-image
numpy
matplotlib
Pillow
confluent-kafka
```

## Installation

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Set up environment variables for database connection in `config/database.py`

3. Run the application:
```bash
uvicorn main:app --reload
```

## Image Processing Pipeline

1. **Image Enhancement**: CLAHE (Contrast Limited Adaptive Histogram Equalization)
2. **Leaf Segmentation**: HSV color space + GrabCut algorithm
3. **Lesion Detection**: Adaptive thresholding + brown color detection
4. **Superpixel Refinement**: SLIC segmentation for improved accuracy
5. **Severity Calculation**: Lesion area / Leaf area ratio

## Usage Example

```python
# Upload image for prediction
import requests

files = {'file': open('rice_leaf.jpg', 'rb')}
data = {'application_id': 'app-123'}

response = requests.post('http://localhost:8000/ai/predict', 
                        files=files, data=data)
result = response.json()

print(f"Disease: {result['data']['predicted_class']}")
print(f"Confidence: {result['data']['confidence']:.2f}%")
print(f"Severity: {result['data']['severity']:.2f}%")
```

## Testing

Run the test script to verify the implementation:
```bash
python test_implementation.py
```

## Kafka Integration

The service listens for application submission events and automatically processes submissions from PCIC provider:

- **Topic**: Application submit events
- **Filter**: Only processes messages where `provider == "PCIC"`
- **Event Structure**: ApplicationSubmittedEvent with objectKeysForAIAnalysis
- **Processing**:
  1. Extracts `objectKeysForAIAnalysis` from the Kafka event
  2. Retrieves each image from MinIO using the object keys
  3. Performs AI prediction on each image (disease classification + severity)
  4. Saves results to database with MinIO object references
  5. Provides comprehensive logging for monitoring

### Kafka Event Structure
```json
{
  "submissionId": "string",
  "provider": "PCIC",
  "objectKeysForAIAnalysis": [
    "applications/{id}/images/rice_leaf_1.jpg",
    "applications/{id}/images/rice_leaf_2.jpg"
  ],
  "documentIds": ["doc1", "doc2"],
  "userId": "string",
  "submittedAt": "2024-11-11T10:30:00"
}
```

## Error Handling

- **Invalid Image**: Returns 400 error for non-image files
- **Model Loading Error**: Service fails to start if model cannot be loaded
- **Database Error**: Proper transaction rollback on failures
- **Prediction Error**: Returns 500 with detailed error message
- **MinIO Errors**: Graceful handling of missing images or connection issues
- **Kafka Processing**: 
  - Individual image failures don't stop batch processing
  - Comprehensive logging for debugging
  - Fallback to mock results when no object keys provided
  - Proper database transaction management

## Files Structure

```
ai_service/
├── main.py                          # FastAPI application
├── requirements.txt                 # Dependencies
├── test_implementation.py           # Test script
├── .gitignore                      # Git ignore rules
├── ai_model/
│   ├── rice_master_model.keras     # Trained model
│   └── __init__.py
├── config/
│   ├── database.py                 # Database configuration
│   └── kafka_config.py             # Kafka configuration
├── controller/
│   └── ai_controller.py            # API endpoints
├── models/
│   └── ai_result.py                # Data models
└── service/
    ├── ai_service.py               # Business logic
    ├── prediction_service.py       # ML prediction logic
    └── kafka_consume_service.py    # Kafka consumer
```

## Notes

- The model expects RGB images and automatically resizes them to the required input size
- Prediction results are saved to the database for audit purposes  
- The service includes visualization capabilities (matplotlib plots)
- Supports both file upload and direct file path prediction (for testing)
- All database operations use proper transaction management
