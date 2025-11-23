import json
import logging
import threading

from confluent_kafka import Consumer
from config.database import get_db
from config.kafka_config import KAFKA_CONFIG, TOPIC_NAME
from service.ai_service import AIService

logger = logging.getLogger(__name__)

def process_ai_analysis(submission_id: str, object_keys: list, user_id: str, db):
    """
    Process AI analysis for a list of object keys from MinIO using the integrated AI service
    """
    logger.info(f"[Kafka] Starting AI analysis for submission: {submission_id}")

    application_data = {
        'submissionId': submission_id,
        'objectKeysForAIAnalysis': object_keys,
        'userId': user_id
    }

    try:
        result = AIService.process_pcic_application(application_data, db)

        logger.info(f"[Kafka] ✅ AI Analysis completed for submission {submission_id}:")
        logger.info(f"  - Result: {result.result}")
        logger.info(f"  - Prediction: {result.prediction}")
        logger.info(f"  - Accuracy: {result.accuracy}")
        logger.info(f"  - Image Path: {result.image_path}")
        logger.info(f"  - Result ID: {result.id}")

        return [result]

    except Exception as e:
        logger.error(f"[Kafka] ❌ Error in AI analysis for submission {submission_id}: {e}")

        # Rollback the current transaction if there was an error
        try:
            db.rollback()
            logger.info(f"[Kafka] Database session rolled back for submission {submission_id}")
        except Exception as rollback_error:
            logger.error(f"[Kafka] Error during rollback: {rollback_error}")

        # Try to download at least one image for severity estimation
        image_bytes = None
        image_path = None
        if object_keys and len(object_keys) > 0:
            try:
                from config.minio_config import MinIOConfig
                minio_config = MinIOConfig()
                first_object_key = object_keys[0]
                logger.info(f"[Kafka] Attempting to download image for severity estimation: {first_object_key}")
                image_bytes = AIService._download_image_from_minio(minio_config, first_object_key)
                image_path = first_object_key
                logger.info(f"[Kafka] Successfully downloaded image for fallback analysis")
            except Exception as download_error:
                logger.warning(f"[Kafka] Failed to download image for fallback: {download_error}")

        # Create default result as fallback with severity estimation if possible
        try:
            fallback_result = AIService.create_ai_result_with_default_prediction(
                submission_id, user_id, db, image_path=image_path, image_bytes=image_bytes
            )
            logger.info(f"[Kafka] Created fallback result: {fallback_result}")
            return [fallback_result]
        except Exception as fallback_error:
            logger.error(f"[Kafka] Failed to create fallback result: {fallback_error}")

            # Try one more time with a complete session refresh
            try:
                db.rollback()
                fallback_result = AIService.create_ai_result_with_default_prediction(
                    submission_id, user_id, db, image_path=image_path, image_bytes=image_bytes
                )
                logger.info(f"[Kafka] Created fallback result after session refresh: {fallback_result}")
                return [fallback_result]
            except Exception as final_error:
                logger.error(f"[Kafka] Final attempt to create fallback result failed: {final_error}")
                return []

def consume_application_submit_event():
    """Main Kafka consumer function"""
    consumer = Consumer(KAFKA_CONFIG)
    consumer.subscribe([TOPIC_NAME])
    logger.info(f"[Kafka] Listening to topic: {TOPIC_NAME}")

    try:
        while True:
            msg = consumer.poll(1.0)
            if msg is None:
                continue
            if msg.error():
                logger.error(f"[Kafka] Error: {msg.error()}")
                continue

            data = msg.value().decode('utf-8')
            logger.info(f"[Kafka] Received message: {data}")

            try:
                payload = json.loads(data)
                logger.info(f"[Kafka] Received data: {payload}")
            except json.JSONDecodeError:
                logger.error("[Kafka] Invalid JSON data")
                payload = {"raw": data}

            handle_message(payload)

    except KeyboardInterrupt:
        logger.info("[Kafka] Consumer stopped manually.")
    finally:
        consumer.close()

def handle_message(message):
    """Process each message and automatically perform AI prediction for PCIC applications"""
    logger.info(f"[Kafka] Processing message: {message}")

    if isinstance(message, dict):
        logger.info("[Kafka] ✅ PCIC application detected - Starting automatic AI prediction")

        submission_id = message.get('submissionId')
        object_keys = message.get('objectKeysForAIAnalysis', [])
        user_id = message.get('userId')

        logger.info(f"[Kafka] Processing submission: {submission_id}")
        logger.info(f"[Kafka] Object keys for analysis: {len(object_keys)} images")
        logger.info(f"[Kafka] User ID: {user_id}")

        try:
            # Get database session
            db_gen = get_db()
            db = next(db_gen)
            try:
                # Automatically perform AI prediction using the process_ai_analysis function
                results = process_ai_analysis(submission_id, object_keys, user_id, db)

                if results:
                    primary_result = results[0]
                    logger.info(f"[Kafka] 🎉 Automatic AI prediction completed successfully!")
                    logger.info(f"[Kafka] Submission ID: {submission_id}")
                    logger.info(f"[Kafka] Predicted Disease: {primary_result.result}")
                    logger.info(f"[Kafka] Confidence: {primary_result.accuracy}")
                    logger.info(f"[Kafka] Severity: {primary_result.severity}%")
                    logger.info(f"[Kafka] AI Result ID: {primary_result.id}")

                    # Log analysis images if available
                    if hasattr(primary_result, 'leaf_analysis_images') and primary_result.leaf_analysis_images:
                        logger.info(f"[Kafka] Generated {len(primary_result.leaf_analysis_images)} analysis images")
                        for img in primary_result.leaf_analysis_images:
                            logger.info(f"[Kafka]   - {img.image_type}: {img.image_path}")

                    # Log top 3 predictions if available
                    if hasattr(primary_result, 'top3_predictions') and primary_result.top3_predictions:
                        logger.info(f"[Kafka] Top 3 predictions:")
                        for pred in primary_result.top3_predictions:
                            logger.info(f"[Kafka]   {pred.rank}. {pred.class_name}: {pred.confidence:.2f}%")
                else:
                    logger.warning(f"[Kafka] No results generated for submission: {submission_id}")
                    # If no results were generated, create a default one with severity estimation
                    try:
                        db.rollback()
                        # Try to download first image for severity estimation
                        image_bytes = None
                        image_path = None
                        if object_keys and len(object_keys) > 0:
                            try:
                                from config.minio_config import MinIOConfig
                                minio_config = MinIOConfig()
                                first_object_key = object_keys[0]
                                logger.info(f"[Kafka] Downloading image for severity estimation: {first_object_key}")
                                image_bytes = AIService._download_image_from_minio(minio_config, first_object_key)
                                image_path = first_object_key
                            except Exception as img_error:
                                logger.warning(f"[Kafka] Failed to download image: {img_error}")

                        default_result = AIService.create_ai_result_with_default_prediction(
                            submission_id,
                            user_id,
                            db,
                            image_path=image_path,
                            image_bytes=image_bytes
                        )
                        logger.info(f"[Kafka] ⚠️ Created default result for submission {submission_id}: {default_result.id}")
                    except Exception as default_error:
                        logger.error(f"[Kafka] Failed to create default result: {default_error}")

            finally:
                try:
                    db.close()
                except Exception as close_error:
                    logger.error(f"[Kafka] Error closing database session: {close_error}")

        except Exception as e:
            logger.error(f"[Kafka] ❌ Error in automatic AI prediction for submission {submission_id}: {str(e)}")
            logger.error(f"[Kafka] Exception details: {type(e).__name__}: {str(e)}")

            # Create fallback result with fresh session
            try:
                db_gen = get_db()
                db = next(db_gen)
                try:
                    # Ensure clean session state
                    db.rollback()

                    # Try to download first image for severity estimation
                    image_bytes = None
                    image_path = None
                    if object_keys and len(object_keys) > 0:
                        try:
                            from config.minio_config import MinIOConfig
                            minio_config = MinIOConfig()
                            first_object_key = object_keys[0]
                            logger.info(f"[Kafka] Downloading image for fallback severity estimation: {first_object_key}")
                            image_bytes = AIService._download_image_from_minio(minio_config, first_object_key)
                            image_path = first_object_key
                        except Exception as img_error:
                            logger.warning(f"[Kafka] Failed to download image for fallback: {img_error}")

                    default_result = AIService.create_ai_result_with_default_prediction(
                        submission_id,
                        user_id,
                        db,
                        image_path=image_path,
                        image_bytes=image_bytes
                    )
                    logger.info(f"[Kafka] ⚠️ Created fallback result for submission {submission_id}: {default_result.id}")
                finally:
                    try:
                        db.close()
                    except Exception as close_error:
                        logger.error(f"[Kafka] Error closing fallback database session: {close_error}")
            except Exception as fallback_error:
                logger.error(f"[Kafka] Failed to create fallback result: {fallback_error}")
                logger.error(f"[Kafka] ❌ CRITICAL: No result saved for submission {submission_id}")
    else:
        provider = message.get('provider', 'Unknown') if isinstance(message, dict) else 'Invalid'
        logger.info(f"[Kafka] ⏭️ Skipping message - Provider: {provider} (Only PCIC applications are processed)")

def start_kafka_consumer():
    """Start Kafka consumer in separate thread for automatic AI prediction"""
    def consumer_thread():
        logger.info("[Kafka] 🚀 Starting automatic AI prediction consumer thread")
        try:
            consume_application_submit_event()
        except Exception as e:
            logger.error(f"[Kafka] Consumer thread error: {str(e)}")

    kafka_thread = threading.Thread(target=consumer_thread, daemon=True)
    kafka_thread.start()
    logger.info("[Kafka] ✅ Automatic AI prediction consumer started in background thread")
    return kafka_thread

def get_consumer_status():
    """Get status information about the Kafka consumer"""
    return {
        "status": "active",
        "topic": TOPIC_NAME,
        "config": {
            "bootstrap_servers": KAFKA_CONFIG.get("bootstrap.servers"),
            "group_id": KAFKA_CONFIG.get("group.id"),
            "auto_offset_reset": KAFKA_CONFIG.get("auto.offset.reset")
        },
        "message": "Kafka consumer is running and processing PCIC applications automatically"
    }
