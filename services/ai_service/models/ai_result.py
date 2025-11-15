from sqlalchemy import Column, Integer, String, Float, ForeignKey, UniqueConstraint
from sqlalchemy.orm import relationship
from config.database import Base
from pydantic import BaseModel
from typing import List, Optional

class AIResult(Base):
    __tablename__ = "ai_result"
    __table_args__ = (UniqueConstraint('applicationId', 'userId', name='uq_application_user'),)
    id = Column(Integer, primary_key=True, index=True)
    applicationId = Column(String)
    userId = Column(String)
    result = Column(String)
    prediction = Column(String)
    accuracy = Column(String)
    confidence = Column(Float)
    severity = Column(Float)
    lesion_area = Column(Float)
    leaf_area = Column(Float)
    image_path = Column(String)

    # Relationship to top 3 predictions
    top3_predictions = relationship("Top3Prediction", back_populates="ai_result", cascade="all, delete-orphan")

    # Relationship to leaf analysis images
    leaf_analysis_images = relationship("LeafAnalysisImage", back_populates="ai_result", cascade="all, delete-orphan")

class Top3Prediction(Base):
    __tablename__ = "top3_prediction"
    id = Column(Integer, primary_key=True, index=True)
    ai_result_id = Column(Integer, ForeignKey("ai_result.id"))
    class_name = Column(String)  # Disease class name
    confidence = Column(Float)  # Confidence score (0-100)
    rank = Column(Integer)  # Rank (1, 2, or 3)

    # Relationship back to AIResult
    ai_result = relationship("AIResult", back_populates="top3_predictions")

class Top3PredictionDTO(BaseModel):
    class_name: str
    confidence: float
    rank: int

    model_config = {'from_attributes': True}

class LeafAnalysisImageDTO(BaseModel):
    image_type: str  # 'isolated_leaf', 'lesion_overlay'
    image_path: str
    presigned_url: str | None = ""  # Allow None and default to empty string
    width: int
    height: int
    file_size: int
    created_at: str

    model_config = {'from_attributes': True}

class AIRequestDTO(BaseModel):
    id: int | None = None
    result: str
    applicationId: str
    userId: str  # User ID from application
    prediction: str
    accuracy: str
    confidence: float
    severity: float
    lesion_area: float
    leaf_area: float
    image_path: str | None = None
    top3_predictions: List[Top3PredictionDTO] = []
    leaf_analysis_images: List[LeafAnalysisImageDTO] = []

    model_config = {'from_attributes': True}

class AIResponseDTO(BaseModel):
    id: int
    result: str
    applicationId: str
    userId: str  # User ID from application
    prediction: str
    accuracy: str
    confidence: float
    severity: float
    lesion_area: float
    leaf_area: float
    image_path: str | None = None
    top3_predictions: List[Top3PredictionDTO] = []
    leaf_analysis_images: List[LeafAnalysisImageDTO] = []

    model_config = {'from_attributes': True}

class LeafAnalysisImage(Base):
    __tablename__ = "leaf_analysis_image"
    id = Column(Integer, primary_key=True, index=True)
    ai_result_id = Column(Integer, ForeignKey("ai_result.id"))
    image_type = Column(String)  # 'isolated_leaf', 'lesion_overlay'
    image_path = Column(String)  # MinIO object name/path for the image
    presigned_url = Column(String)  # Presigned URL for accessing the image
    width = Column(Integer)  # Image width in pixels
    height = Column(Integer)  # Image height in pixels
    file_size = Column(Integer)  # File size in bytes
    created_at = Column(String)  # Creation timestamp

    # Relationship back to AIResult
    ai_result = relationship("AIResult", back_populates="leaf_analysis_images")
