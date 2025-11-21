import os
from minio import Minio
from minio.error import S3Error
from typing import Optional
from datetime import timedelta
import logging

# MinIO Configuration
MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "localhost:9000")
MINIO_ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY", "minio")
MINIO_SECRET_KEY = os.getenv("MINIO_SECRET_KEY", "minio123")
MINIO_SECURE = os.getenv("MINIO_SECURE", "false").lower() == "true"

# Default bucket for AI service
DEFAULT_BUCKET = os.getenv("MINIO_BUCKET", "ai-service-bucket")

# Document bucket for reading uploaded documents
DOCUMENT_BUCKET = os.getenv("DOCUMENT_BUCKET", "documents")

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class MinIOConfig:
    """MinIO configuration and client management"""

    def __init__(self):
        self.client = None
        self.connect()

    def connect(self):
        """Initialize MinIO client connection"""
        try:
            self.client = Minio(
                endpoint=MINIO_ENDPOINT,
                access_key=MINIO_ACCESS_KEY,
                secret_key=MINIO_SECRET_KEY,
                secure=MINIO_SECURE
            )
            logger.info(f"MinIO client connected to {MINIO_ENDPOINT}")

            # Create default bucket if it doesn't exist
            self.create_bucket_if_not_exists(DEFAULT_BUCKET)

        except Exception as e:
            logger.error(f"Failed to connect to MinIO: {e}")
            self.client = None

    def create_bucket_if_not_exists(self, bucket_name: str):
        """Create bucket if it doesn't exist"""
        try:
            if not self.client:
                return
            if not self.client.bucket_exists(bucket_name):
                self.client.make_bucket(bucket_name)
                logger.info(f"Created bucket: {bucket_name}")
            else:
                logger.info(f"Bucket already exists: {bucket_name}")
        except Exception as e:
            logger.error(f"Error creating bucket {bucket_name}: {e}")

    def upload_file(self, bucket_name: str, object_name: str, file_path: str, content_type: str = None):
        """Upload file to MinIO"""
        try:
            if not self.client:
                return False

            result = self.client.fput_object(
                bucket_name, object_name, file_path,
                content_type=content_type
            )
            logger.info(f"File uploaded successfully: {result}")
            return True
        except Exception as e:
            logger.error(f"Error uploading file: {e}")
            return False

    def get_presigned_url(self, bucket_name: str, object_name: str, expires: int = 3600):
        """Get presigned URL for object"""
        try:
            if not self.client:
                return None

            url = self.client.presigned_get_object(bucket_name, object_name, expires=timedelta(seconds=expires))
            return url
        except Exception as e:
            logger.error(f"Error getting presigned URL: {e}")
            return None

    def download_object(self, bucket_name: str, object_name: str):
        """Download object from MinIO"""
        try:
            if not self.client:
                return None

            response = self.client.get_object(bucket_name, object_name)
            data = response.read()
            response.close()
            response.release_conn()
            return data
        except Exception as e:
            logger.error(f"Error downloading object: {e}")
            return None

# Global instance
minio_client = MinIOConfig()
