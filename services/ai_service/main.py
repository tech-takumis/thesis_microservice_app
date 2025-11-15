from fastapi import FastAPI
from config.database import Base, engine
from controller import ai_controller
from service.kafka_consume_service import start_kafka_consumer
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('ai_service.log')
    ]
)

# Set specific logger levels
logging.getLogger("service.prediction_service").setLevel(logging.INFO)
logging.getLogger("service.ai_service").setLevel(logging.INFO)

app = FastAPI(
    title="AI Service",
    description="Rice disease prediction service using machine learning",
    version="1.0.0"
)

# Create database tables
Base.metadata.create_all(bind=engine)

# Include routers
app.include_router(ai_controller.router)

# Start Kafka consumer on startup
@app.on_event("startup")
async def startup_event():
    """Start background services"""
    try:
        start_kafka_consumer()
        logging.info("AI Service started successfully")
    except Exception as e:
        logging.error(f"Failed to start background services: {e}")

@app.get("/")
def read_root():
    return {"message": "AI Service is running", "status": "healthy"}

@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "ai-service"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
