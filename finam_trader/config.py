"""
Configuration management for Finam Trade API (gRPC)
"""

import os
from pathlib import Path
from dotenv import load_dotenv

# Load .env from the finam_trader directory
env_path = Path(__file__).parent / '.env'
load_dotenv(dotenv_path=env_path)

# Finam Trade API Configuration
FINAM_API_TOKEN = os.getenv("FINAM_API_TOKEN")
# gRPC server URL
FINAM_API_URL = os.getenv("FINAM_API_URL", "https://tradeapi.finam.ru")

# Monitoring Configuration
DIVERGENCE_THRESHOLD_PERCENT = float(os.getenv("DIVERGENCE_THRESHOLD_PERCENT", "1.0"))
MONITORING_INTERVAL_SECONDS = int(os.getenv("MONITORING_INTERVAL_SECONDS", "60"))

# Request Configuration
REQUEST_TIMEOUT = int(os.getenv("FINAM_REQUEST_TIMEOUT", "30"))

def validate_config():
    """Validate required configuration is present"""
    if not FINAM_API_TOKEN:
        raise ValueError(
            "FINAM_API_TOKEN not found in environment variables. "
            "Please set it in your .env file or environment."
        )
    return True
