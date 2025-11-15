import os

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:29092")
KAFKA_GROUP_ID = os.getenv("KAFKA_GROUP_ID", "ai-service-group")
TOPIC_NAME = os.getenv("KAFKA_TOPIC", "application-submitted")

KAFKA_CONFIG = {
    'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS,
    'group.id': KAFKA_GROUP_ID,
    'auto.offset.reset': 'earliest',
    'enable.auto.commit': True,
    'session.timeout.ms': 6000,
    'heartbeat.interval.ms': 3000
}
