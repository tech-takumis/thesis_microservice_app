from sqlalchemy.orm import Session
from models.ai_result import AIResult, AIResponseDTO, AIRequestDTO
from service.prediction_service import PredictionService
from config.minio_config import MinIOConfig
from typing import Dict, Optional, List
import uuid
import io
import logging

logger = logging.getLogger(__name__)

class AIService:

    @staticmethod
    def get_all_ai_results(db: Session):
        """Get all AI results from database"""
        results = db.query(AIResult).all()
        return [AIResponseDTO.model_validate(r) for r in results]

    @staticmethod
    def add_ai_result(db: Session, result: AIRequestDTO) -> AIResponseDTO:
        """Add new AI result to database"""
        from models.ai_result import Top3Prediction, LeafAnalysisImage

        # Create AIResult instance from DTO
        db_result = AIResult(
            applicationId=result.applicationId,
            userId=result.userId,
            result=result.result,
            prediction=result.prediction,
            accuracy=result.accuracy,
            confidence=result.confidence,
            severity=result.severity,
            lesion_area=result.lesion_area,
            leaf_area=result.leaf_area,
            image_path=result.image_path
        )
        db.add(db_result)
        db.flush()  # Flush to get the ID

        # Create Top3Prediction records
        for top3_pred in result.top3_predictions:
            db_top3 = Top3Prediction(
                ai_result_id=db_result.id,
                class_name=top3_pred.class_name,
                confidence=top3_pred.confidence,
                rank=top3_pred.rank
            )
            db.add(db_top3)

        # Create LeafAnalysisImage records
        for leaf_img in result.leaf_analysis_images:
            db_leaf_img = LeafAnalysisImage(
                ai_result_id=db_result.id,
                image_type=leaf_img.image_type,
                image_path=leaf_img.image_path,
                presigned_url=leaf_img.presigned_url,
                width=leaf_img.width,
                height=leaf_img.height,
                file_size=leaf_img.file_size,
                created_at=leaf_img.created_at
            )
            db.add(db_leaf_img)

        db.commit()
        db.refresh(db_result)
        return AIResponseDTO.model_validate(db_result)

    @staticmethod
    def get_ai_result_by_id(db: Session, result_id: int) -> Optional[AIResponseDTO]:
        """Get AI result by ID"""
        result = db.query(AIResult).filter(AIResult.id == result_id).first()
        if result is None:
            return None
        return AIResponseDTO.model_validate(result)

    @staticmethod
    def get_ai_result_by_application_id(db: Session, application_id: str) -> Optional[AIResponseDTO]:
        """Get AI result for a specific application ID"""
        result = db.query(AIResult).filter(AIResult.applicationId == application_id).first()
        if result is None:
            return None
        return AIResponseDTO.model_validate(result)

    @staticmethod
    def predict_rice_disease(image_bytes: bytes, application_id: str, user_id: str, db: Session, filename: str = None) -> Dict:
        """
        Predict rice disease from image, save to MinIO and database
        """
        try:
            # Initialize services
            prediction_service = PredictionService()
            minio_config = MinIOConfig()

            # Generate unique filename if not provided
            if not filename:
                filename = f"{uuid.uuid4()}.jpg"

            # Upload image to MinIO
            object_name = f"applications/{application_id}/{filename}"
            upload_success = AIService._upload_image_to_minio(
                minio_config, image_bytes, object_name
            )

            if not upload_success:
                raise Exception("Failed to upload image to storage")

            # Make prediction
            prediction_result = prediction_service.predict_disease_from_bytes(image_bytes)

            # Convert top3_predictions to DTOs
            from models.ai_result import Top3PredictionDTO, LeafAnalysisImageDTO
            top3_dtos = [
                Top3PredictionDTO(
                    class_name=pred["class_name"],
                    confidence=pred["confidence"],
                    rank=pred["rank"]
                ) for pred in prediction_result["top3_predictions"]
            ]

            # Convert analysis_images to DTOs
            analysis_image_dtos = [
                LeafAnalysisImageDTO(
                    image_type=img["image_type"],
                    image_path=img["image_path"],
                    presigned_url=img["presigned_url"],
                    width=img["width"],
                    height=img["height"],
                    file_size=img["file_size"],
                    created_at=img["created_at"]
                ) for img in prediction_result.get("analysis_images", [])
            ]

            # Save result to database
            ai_request = AIRequestDTO(
                applicationId=application_id,
                userId=user_id,
                result=prediction_result["predicted_class"],
                prediction=f"Disease: {prediction_result['predicted_class']}, Severity: {prediction_result['lesion_ratio']:.2f}%",
                accuracy=f"{prediction_result['confidence']:.2f}%",
                confidence=prediction_result["confidence"],
                severity=prediction_result["lesion_ratio"],
                lesion_area=prediction_result["lesion_area"],
                leaf_area=prediction_result["leaf_area"],
                image_path=object_name,
                top3_predictions=top3_dtos,
                leaf_analysis_images=analysis_image_dtos
            )

            saved_result = AIService.add_ai_result(db, ai_request)

            # Get presigned URL for the uploaded image
            image_url = AIService._get_image_url(minio_config, object_name, expires_in_seconds=7200)

            # Return combined result
            return {
                "id": saved_result.id,
                "predicted_class": prediction_result["predicted_class"],
                "confidence": prediction_result["confidence"],
                "severity": prediction_result["lesion_ratio"],
                "application_id": application_id,
                "image_url": image_url,
                "object_name": object_name
            }

        except Exception as e:
            logger.error(f"Prediction failed: {str(e)}")
            raise Exception(f"Prediction failed: {str(e)}")

    @staticmethod
    def create_ai_result_with_default_prediction(application_id: str, user_id: str, db: Session, image_path: str = None) -> AIResponseDTO:
        """
        Create an AI result with default/unknown prediction when no image analysis can be performed
        """
        from models.ai_result import Top3PredictionDTO

        ai_request = AIRequestDTO(
            applicationId=application_id,
            userId=user_id,
            result="Unknown",
            prediction="Disease: Unknown - No image analysis performed",
            accuracy="0.00%",
            confidence=0.0,
            severity=0.0,
            lesion_area=0.0,
            leaf_area=0.0,
            image_path=image_path,
            top3_predictions=[
                Top3PredictionDTO(class_name="Unknown", confidence=0.0, rank=1),
                Top3PredictionDTO(class_name="Unknown", confidence=0.0, rank=2),
                Top3PredictionDTO(class_name="Unknown", confidence=0.0, rank=3)
            ],
            leaf_analysis_images=[]
        )
        return AIService.add_ai_result(db, ai_request)

    @staticmethod
    def process_pcic_application(application_data: Dict, db: Session) -> AIResponseDTO:
        """
        Process PCIC application and perform AI analysis
        """
        try:
            application_id = application_data.get('submissionId')
            user_id = application_data.get('userId')
            object_keys = application_data.get('objectKeysForAIAnalysis', [])

            if not object_keys:
                logger.warning(f"No images found for application {application_id}, creating default result")
                return AIService.create_ai_result_with_default_prediction(application_id, user_id, db)

            # Initialize services
            minio_config = MinIOConfig()
            prediction_service = PredictionService()

            # Process all images and get the best result
            best_result = None
            highest_confidence = 0.0

            for i, object_key in enumerate(object_keys):
                try:
                    logger.info(f"Processing image {i+1}/{len(object_keys)}: {object_key}")

                    # Download image from MinIO
                    image_bytes = AIService._download_image_from_minio(minio_config, object_key)

                    if not image_bytes:
                        logger.error(f"Failed to download image {object_key}")
                        continue

                    # Make prediction
                    prediction_result = prediction_service.predict_disease_from_bytes(image_bytes)

                    # Keep the result with highest confidence
                    if prediction_result["confidence"] > highest_confidence:
                        highest_confidence = prediction_result["confidence"]
                        best_result = {
                            "prediction_result": prediction_result,
                            "object_key": object_key
                        }

                    logger.info(f"Processed {object_key}: {prediction_result['predicted_class']} ({prediction_result['confidence']:.2f}%)")

                except Exception as img_error:
                    logger.error(f"Error processing image {object_key}: {str(img_error)}")
                    continue

            # Use the best result if we have one
            if best_result:
                prediction_result = best_result["prediction_result"]
                object_key = best_result["object_key"]

                # Convert top3_predictions to DTOs
                from models.ai_result import Top3PredictionDTO, LeafAnalysisImageDTO
                top3_dtos = [
                    Top3PredictionDTO(
                        class_name=pred["class_name"],
                        confidence=pred["confidence"],
                        rank=pred["rank"]
                    ) for pred in prediction_result["top3_predictions"]
                ]

                # Convert analysis_images to DTOs
                analysis_image_dtos = [
                    LeafAnalysisImageDTO(
                        image_type=img["image_type"],
                        image_path=img["image_path"],
                        presigned_url=img["presigned_url"],
                        width=img["width"],
                        height=img["height"],
                        file_size=img["file_size"],
                        created_at=img["created_at"]
                    ) for img in prediction_result.get("analysis_images", [])
                ]

                # Save result to database
                ai_request = AIRequestDTO(
                    applicationId=application_id,
                    userId=user_id,
                    result=prediction_result["predicted_class"],
                    prediction=f"Disease: {prediction_result['predicted_class']}, Severity: {prediction_result['lesion_ratio']:.2f}%",
                    accuracy=f"{prediction_result['confidence']:.2f}%",
                    confidence=prediction_result["confidence"],
                    severity=prediction_result["lesion_ratio"],
                    lesion_area=prediction_result["lesion_area"],
                    leaf_area=prediction_result["leaf_area"],
                    image_path=object_key,
                    top3_predictions=top3_dtos,
                    leaf_analysis_images=analysis_image_dtos
                )

                return AIService.add_ai_result(db, ai_request)
            else:
                # No images could be processed
                logger.warning(f"No images could be processed for application {application_id}")
                return AIService.create_ai_result_with_default_prediction(application_id, user_id, db)

        except Exception as e:
            logger.error(f"Failed to process PCIC application {application_data.get('submissionId')}: {str(e)}")
            # Fallback to default result
            return AIService.create_ai_result_with_default_prediction(application_data.get('submissionId'), application_data.get('userId'), db)

    @staticmethod
    def analyze_single_image(image_bytes: bytes, application_id: str, user_id: str, db: Session, object_key: str = None) -> AIResponseDTO:
        """
        Analyze a single image and save the result to database
        """
        try:
            logger.info(f"Starting single image analysis for application: {application_id}")
            logger.info(f"Image bytes size: {len(image_bytes)} bytes")
            logger.info(f"Object key: {object_key}")

            # Initialize services
            prediction_service = PredictionService()
            logger.info("PredictionService initialized")

            # Make prediction
            logger.info("Making prediction on image...")
            prediction_result = prediction_service.predict_disease_from_bytes(image_bytes)
            logger.info(f"Prediction result: {prediction_result}")

            # Convert top3_predictions to DTOs
            from models.ai_result import Top3PredictionDTO, LeafAnalysisImageDTO
            top3_dtos = [
                Top3PredictionDTO(
                    class_name=pred["class_name"],
                    confidence=pred["confidence"],
                    rank=pred["rank"]
                ) for pred in prediction_result["top3_predictions"]
            ]

            # Convert analysis_images to DTOs
            analysis_image_dtos = [
                LeafAnalysisImageDTO(
                    image_type=img["image_type"],
                    image_path=img["image_path"],
                    presigned_url=img["presigned_url"],
                    width=img["width"],
                    height=img["height"],
                    file_size=img["file_size"],
                    created_at=img["created_at"]
                ) for img in prediction_result.get("analysis_images", [])
            ]

            # Save result to database
            ai_request = AIRequestDTO(
                applicationId=application_id,
                userId=user_id,
                result=prediction_result["predicted_class"],
                prediction=f"Disease: {prediction_result['predicted_class']}, Severity: {prediction_result['lesion_ratio']:.2f}%",
                accuracy=f"{prediction_result['confidence']:.2f}%",
                confidence=prediction_result["confidence"],
                severity=prediction_result["lesion_ratio"],
                lesion_area=prediction_result["lesion_area"],
                leaf_area=prediction_result["leaf_area"],
                image_path=object_key,
                top3_predictions=top3_dtos,
                leaf_analysis_images=analysis_image_dtos
            )
            logger.info(f"Created AIRequestDTO: {ai_request}")

            saved_result = AIService.add_ai_result(db, ai_request)
            logger.info(f"Successfully saved AI result with ID: {saved_result.id}")
            return saved_result

        except Exception as e:
            logger.error(f"Failed to analyze single image: {str(e)}", exc_info=True)
            logger.info("Falling back to default prediction")
            # Return default prediction as fallback
            return AIService.create_ai_result_with_default_prediction(application_id, user_id, db, object_key)

    @staticmethod
    def get_prediction_statistics(db: Session, application_id: str = None) -> Dict:
        """
        Get statistics about predictions made by the AI service
        """
        try:
            query = db.query(AIResult)
            if application_id:
                query = query.filter(AIResult.applicationId == application_id)

            results = query.all()

            if not results:
                return {
                    "total_predictions": 0,
                    "disease_distribution": {},
                    "average_confidence": 0.0,
                    "unknown_predictions": 0
                }

            total = len(results)
            disease_counts = {}
            confidence_scores = []
            unknown_count = 0

            for result in results:
                disease = result.result
                if disease == "Unknown":
                    unknown_count += 1
                else:
                    disease_counts[disease] = disease_counts.get(disease, 0) + 1

                # Extract confidence from accuracy field
                try:
                    if result.accuracy and result.accuracy != "0.00%":
                        confidence = float(result.accuracy.replace('%', ''))
                        confidence_scores.append(confidence)
                except:
                    pass

            avg_confidence = sum(confidence_scores) / len(confidence_scores) if confidence_scores else 0.0

            return {
                "total_predictions": total,
                "disease_distribution": disease_counts,
                "average_confidence": round(avg_confidence, 2),
                "unknown_predictions": unknown_count,
                "success_rate": round(((total - unknown_count) / total * 100), 2) if total > 0 else 0.0
            }

        except Exception as e:
            logger.error(f"Failed to get prediction statistics: {str(e)}")
            return {
                "error": str(e),
                "total_predictions": 0,
                "disease_distribution": {},
                "average_confidence": 0.0,
                "unknown_predictions": 0
            }

    @staticmethod
    def _upload_image_to_minio(minio_config: MinIOConfig, image_bytes: bytes, object_name: str) -> bool:
        """Upload image bytes to MinIO"""
        try:
            if not minio_config.client:
                logger.error("MinIO client not available")
                return False

            # Convert bytes to file-like object
            image_stream = io.BytesIO(image_bytes)

            # Upload to MinIO
            minio_config.client.put_object(
                bucket_name="ai-service-bucket",
                object_name=object_name,
                data=image_stream,
                length=len(image_bytes),
                content_type='image/jpeg'
            )

            logger.info(f"Successfully uploaded image to MinIO: {object_name}")
            return True

        except Exception as e:
            logger.error(f"Failed to upload image to MinIO: {str(e)}")
            return False

    @staticmethod
    def _get_image_url(minio_config: MinIOConfig, object_name: str, expires_in_seconds: int = 3600) -> Optional[str]:
        """Get presigned URL for image from MinIO"""
        try:
            if not minio_config.client:
                logger.error("MinIO client not available")
                return None

            url = minio_config.client.presigned_get_object(
                bucket_name="ai-service-bucket",
                object_name=object_name,
                expires=expires_in_seconds
            )

            return url

        except Exception as e:
            logger.error(f"Failed to get presigned URL: {str(e)}")
            return None

    @staticmethod
    def _download_image_from_minio(minio_config: MinIOConfig, object_name: str) -> Optional[bytes]:
        """Download image from MinIO as bytes"""
        try:
            if not minio_config.client:
                logger.error("MinIO client not available")
                return None

            response = minio_config.client.get_object(
                bucket_name="ai-service-bucket",
                object_name=object_name
            )

            image_bytes = response.read()
            response.close()
            response.release_conn()

            logger.info(f"Successfully downloaded image from MinIO: {object_name}")
            return image_bytes

        except Exception as e:
            logger.error(f"Failed to download image from MinIO: {str(e)}")
            return None
