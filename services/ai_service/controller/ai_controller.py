from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.orm import Session
from config.database import get_db
from models.ai_result import AIRequestDTO, AIResponseDTO, AIResult
from service.ai_service import AIService
from typing import Dict, Optional

router = APIRouter(prefix="/ai", tags=["ai"])

def format_ai_result_response(result: AIResponseDTO) -> Dict:
    """Helper function to format AI result into complete response structure"""
    # Generate presigned URL for original image from documents bucket
    original_image_url = AIService.generate_presigned_url_for_original_image(result.image_path)

    # Generate presigned URLs for leaf analysis images on-the-fly
    leaf_analysis_images = []
    for img in result.leaf_analysis_images:
        presigned_url = AIService.generate_presigned_url_for_analysis_image(img.image_path)
        leaf_analysis_images.append({
            "image_type": img.image_type,
            "image_path": img.image_path,
            "presigned_url": presigned_url,
            "width": img.width,
            "height": img.height,
            "file_size": img.file_size,
            "created_at": img.created_at
        })

    return {
        "id": result.id,
        "result": result.result,
        "applicationId": result.applicationId,
        "userId": result.userId,
        "prediction": result.prediction,
        "accuracy": result.accuracy,
        "confidence": result.confidence,
        "severity": result.severity,
        "lesion_area": result.lesion_area,
        "leaf_area": result.leaf_area,
        "image_path": result.image_path,
        "original_image_url": original_image_url,
        "top3_predictions": [
            {
                "class_name": pred.class_name,
                "confidence": pred.confidence,
                "rank": pred.rank
            } for pred in result.top3_predictions
        ],
        "leaf_analysis_images": leaf_analysis_images
    }

@router.get("/", response_model=Dict)
def get_results(db: Session = Depends(get_db)):
    """Get all AI results with complete data structure"""
    try:
        results = AIService.get_all_ai_results(db)
        formatted_results = [format_ai_result_response(result) for result in results]

        return {
            "success": True,
            "data": formatted_results,
            "message": f"Retrieved {len(formatted_results)} AI results successfully"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get results: {str(e)}")

@router.post("/", response_model=Dict)
def add_result(result: AIRequestDTO, db: Session = Depends(get_db)):
    """Add new AI result and return complete data structure"""
    try:
        saved_result = AIService.add_ai_result(db, result)

        return {
            "success": True,
            "data": format_ai_result_response(saved_result),
            "message": "AI result added successfully"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to add result: {str(e)}")

@router.get("/{result_id}", response_model=Dict)
def get_result_by_id(result_id: int, db: Session = Depends(get_db)):
    """Get AI result by ID with complete data structure"""
    try:
        result = AIService.get_ai_result_by_id(db, result_id)
        if result is None:
            raise HTTPException(status_code=404, detail="Result not found")

        return {
            "success": True,
            "data": format_ai_result_response(result),
            "message": "AI result retrieved successfully"
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get result: {str(e)}")

@router.post("/predict", response_model=Dict)
async def predict_rice_disease(
    file: UploadFile = File(...),
    application_id: str = Form(...),
    user_id: str = Form(...),
    db: Session = Depends(get_db)
):
    """
    Predict rice disease from uploaded image
    """
    # Validate file type
    if not file.content_type.startswith('image/'):
        raise HTTPException(status_code=400, detail="File must be an image")

    try:
        # Read image bytes
        image_bytes = await file.read()

        # Make prediction and save to database
        saved_result = AIService.predict_rice_disease(image_bytes, application_id, user_id, db, file.filename)

        # Get the complete result from database
        if isinstance(saved_result, dict) and 'id' in saved_result:
            # If predict_rice_disease returns dict with id, get from database
            complete_result = AIService.get_ai_result_by_id(db, saved_result['id'])
            if complete_result:
                return {
                    "success": True,
                    "data": format_ai_result_response(complete_result),
                    "message": "Disease prediction completed successfully"
                }

        return {
            "success": True,
            "data": saved_result,
            "message": "Disease prediction completed successfully"
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")


@router.get("/statistics", response_model=Dict)
def get_prediction_statistics(
    application_id: Optional[str] = None,
    db: Session = Depends(get_db)
):
    """
    Get statistics about predictions made by the AI service
    """
    try:
        stats = AIService.get_prediction_statistics(db, application_id)
        return {
            "success": True,
            "data": stats,
            "message": "Statistics retrieved successfully"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get statistics: {str(e)}")


@router.get("/{result_id}/image", response_model=Dict)
def get_result_image(result_id: int, db: Session = Depends(get_db)):
    """
    Get the original image URL for a specific AI result
    """
    try:
        result = AIService.get_ai_result_by_id(db, result_id)
        if result is None:
            raise HTTPException(status_code=404, detail="Result not found")

        if not result.image_path:
            raise HTTPException(status_code=404, detail="No image associated with this result")

        # Get presigned URL for the image using MinIO config
        from config.minio_config import MinIOConfig
        from datetime import timedelta

        minio_config = MinIOConfig()
        if minio_config.client:
            try:
                image_url = minio_config.client.presigned_get_object(
                    bucket_name="ai-service-bucket",
                    object_name=result.image_path,
                    expires=timedelta(seconds=3600)
                )

                return {
                    "success": True,
                    "data": {
                        "image_url": image_url,
                        "expires_in": 3600,
                        "image_path": result.image_path,
                        "result_info": format_ai_result_response(result)
                    },
                    "message": "Image URL retrieved successfully"
                }
            except Exception as e:
                raise HTTPException(status_code=500, detail=f"Failed to generate image URL: {str(e)}")
        else:
            raise HTTPException(status_code=500, detail="MinIO client not available")

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get image: {str(e)}")

@router.get("/{result_id}/analysis-images", response_model=Dict)
def get_analysis_images(result_id: int, db: Session = Depends(get_db)):
    """
    Get leaf analysis images (isolated leaf and lesion overlay) for a specific AI result
    """
    try:
        from models.ai_result import LeafAnalysisImage

        result = AIService.get_ai_result_by_id(db, result_id)
        if result is None:
            raise HTTPException(status_code=404, detail="Result not found")

        # Get analysis images
        analysis_images = db.query(LeafAnalysisImage).filter(
            LeafAnalysisImage.ai_result_id == result_id
        ).all()

        images_data = []
        for img in analysis_images:
            # Generate presigned URL on-the-fly
            presigned_url = AIService.generate_presigned_url_for_analysis_image(img.image_path)
            images_data.append({
                "id": img.id,
                "image_type": img.image_type,
                "image_path": img.image_path,
                "presigned_url": presigned_url,
                "width": img.width,
                "height": img.height,
                "file_size": img.file_size,
                "created_at": img.created_at
            })

        return {
            "success": True,
            "data": {
                "result_id": result_id,
                "total_images": len(images_data),
                "analysis_images": images_data
            },
            "message": "Analysis images retrieved successfully"
        }

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get analysis images: {str(e)}")


@router.get("/applications/{application_id}")
def get_result_by_application_id(application_id: str, db: Session = Depends(get_db)):
    """Get AI results by Application ID with complete data structure"""
    try:
        result = AIService.get_ai_result_by_application_id(db, application_id)
        formatted_results = format_ai_result_response(result)

        return {
            "success": True,
            "data": formatted_results,
            "message": f"Retrieved {len(formatted_results)} AI results for application ID {application_id} successfully"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get results: {str(e)}")
