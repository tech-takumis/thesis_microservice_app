from sqlalchemy.orm import Session
from models.ai_result import AIResult, AIResponseDTO, AIRequestDTO
from service.prediction_service import PredictionService
from config.minio_config import MinIOConfig, DOCUMENT_BUCKET
from typing import Dict, Optional
from datetime import timedelta
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
        if result is not None:
            return AIResponseDTO.model_validate(result)
        return None

    @staticmethod
    def get_ai_result_by_application_id(db: Session, application_id: str) -> Optional[AIResponseDTO]:
        """Get AI result for a specific application ID"""
        result = db.query(AIResult).filter(AIResult.applicationId == application_id).first()
        if result is not None:
            return AIResponseDTO.model_validate(result)
        return None

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
    def create_ai_result_with_default_prediction(
        application_id: str,
        user_id: str,
        db: Session,
        image_path: str = None,
        image_bytes: bytes = None
    ) -> AIResponseDTO:
        """
        Create an AI result with default/unknown prediction when disease classification fails
        Still attempts to run RICEMASTER_PERFECTION to get severity estimation if image_bytes provided
        """
        from models.ai_result import Top3PredictionDTO, LeafAnalysisImageDTO

        severity = 0.0
        lesion_area = 0.0
        leaf_area = 0.0
        analysis_image_dtos = []
        prediction_text = "Disease: Unknown - Classification failed"

        # If image bytes are provided, try to get severity estimation using RICEMASTER_PERFECTION
        if image_bytes:
            try:
                logger.info(f"Attempting severity estimation for application {application_id}")
                prediction_service = PredictionService()

                # Run RICEMASTER_PERFECTION to get severity metrics
                prediction_result = prediction_service.predict_disease_from_bytes(image_bytes)

                # Extract severity metrics even though we'll mark disease as Unknown
                severity = prediction_result.get("lesion_ratio", 0.0)
                lesion_area = prediction_result.get("lesion_area", 0.0)
                leaf_area = prediction_result.get("leaf_area", 0.0)

                # Convert analysis_images to DTOs if available
                analysis_image_dtos = [
                    LeafAnalysisImageDTO(
                        image_type=img["image_type"],
                        image_path=img["image_path"],
                        width=img["width"],
                        height=img["height"],
                        file_size=img["file_size"],
                        created_at=img["created_at"]
                    ) for img in prediction_result.get("analysis_images", [])
                ]

                prediction_text = f"Disease: Unknown - Classification failed, Severity: {severity:.2f}%"
                logger.info(f"Severity estimation successful: {severity:.2f}%")

            except Exception as e:
                logger.warning(f"Severity estimation also failed for {application_id}: {str(e)}")
                prediction_text = "Disease: Unknown - No analysis could be performed"

        ai_request = AIRequestDTO(
            applicationId=application_id,
            userId=user_id,
            result="Unknown",
            prediction=prediction_text,
            accuracy="0.00%",
            confidence=0.0,
            severity=severity,
            lesion_area=lesion_area,
            leaf_area=leaf_area,
            image_path=image_path,
            top3_predictions=[
                Top3PredictionDTO(class_name="Unknown", confidence=0.0, rank=1),
                Top3PredictionDTO(class_name="Unknown", confidence=0.0, rank=2),
                Top3PredictionDTO(class_name="Unknown", confidence=0.0, rank=3)
            ],
            leaf_analysis_images=analysis_image_dtos
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
                # No images could be processed, try to at least get severity estimation
                logger.warning(f"No images could be processed for application {application_id}")

                # Try to download first image for severity estimation
                image_bytes = None
                image_path = None
                if object_keys and len(object_keys) > 0:
                    try:
                        first_object_key = object_keys[0]
                        logger.info(f"Attempting to download first image for severity estimation: {first_object_key}")
                        image_bytes = AIService._download_image_from_minio(minio_config, first_object_key)
                        image_path = first_object_key
                    except Exception as download_error:
                        logger.warning(f"Failed to download image for severity estimation: {download_error}")

                return AIService.create_ai_result_with_default_prediction(
                    application_id, user_id, db, image_path=image_path, image_bytes=image_bytes
                )

        except Exception as e:
            logger.error(f"Failed to process PCIC application {application_data.get('submissionId')}: {str(e)}")

            # Fallback to default result - try to download first image for severity estimation
            application_id = application_data.get('submissionId')
            user_id = application_data.get('userId')
            object_keys = application_data.get('objectKeysForAIAnalysis', [])

            image_bytes = None
            image_path = None
            if object_keys and len(object_keys) > 0:
                try:
                    minio_config = MinIOConfig()
                    first_object_key = object_keys[0]
                    logger.info(f"Attempting to download first image for fallback severity estimation: {first_object_key}")
                    image_bytes = AIService._download_image_from_minio(minio_config, first_object_key)
                    image_path = first_object_key
                except Exception as download_error:
                    logger.warning(f"Failed to download image for fallback: {download_error}")

            return AIService.create_ai_result_with_default_prediction(
                application_id, user_id, db, image_path=image_path, image_bytes=image_bytes
            )

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
            logger.info("Falling back to default prediction with severity estimation")
            # Return default prediction as fallback, still attempt severity estimation
            return AIService.create_ai_result_with_default_prediction(
                application_id, user_id, db, image_path=object_key, image_bytes=image_bytes
            )

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
    def generate_presigned_url_for_analysis_image(image_path: str, expires_in_seconds: int = 7200) -> Optional[str]:
        """
        Generate presigned URL for analysis image from MinIO
        Analysis images are stored in ai-service-bucket
        """
        try:
            minio_config = MinIOConfig()
            if not minio_config.client:
                logger.error("MinIO client not available")
                return None

            url = minio_config.client.presigned_get_object(
                bucket_name="ai-service-bucket",
                object_name=image_path,
                expires=timedelta(seconds=expires_in_seconds)
            )

            return url

        except Exception as e:
            logger.error(f"Failed to generate presigned URL for {image_path}: {str(e)}")
            return None

    @staticmethod
    def generate_presigned_url_for_original_image(image_path: str, expires_in_seconds: int = 7200) -> Optional[str]:
        """
        Generate presigned URL for original uploaded image from MinIO
        Original images are stored in documents bucket
        """
        try:
            if not image_path:
                return None

            minio_config = MinIOConfig()
            if not minio_config.client:
                logger.error("MinIO client not available")
                return None

            # Original images are stored in the documents bucket
            url = minio_config.client.presigned_get_object(
                bucket_name=DOCUMENT_BUCKET,
                object_name=image_path,
                expires=timedelta(seconds=expires_in_seconds)
            )

            return url

        except Exception as e:
            logger.error(f"Failed to generate presigned URL for original image {image_path}: {str(e)}")
            return None

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
                expires=timedelta(seconds=expires_in_seconds)
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

            # Try multiple bucket names in order of preference
            bucket_names_to_try = [
                DOCUMENT_BUCKET,
                "documents",
                "document-service-bucket",
                "uploads",
                "ai-service-bucket"  # fallback to original
            ]

            for bucket_name in bucket_names_to_try:
                try:
                    logger.info(f"Trying to download from bucket: {bucket_name}")
                    response = minio_config.client.get_object(
                        bucket_name=bucket_name,
                        object_name=object_name
                    )

                    image_bytes = response.read()
                    response.close()
                    response.release_conn()

                    logger.info(f"Successfully downloaded image from MinIO bucket '{bucket_name}': {object_name}")
                    return image_bytes

                except Exception as bucket_error:
                    logger.warning(f"Failed to download from bucket '{bucket_name}': {str(bucket_error)}")
                    continue

            logger.error(f"Failed to download image '{object_name}' from any bucket")
            return None

        except Exception as e:
            logger.error(f"Failed to download image from MinIO: {str(e)}")
            return None
