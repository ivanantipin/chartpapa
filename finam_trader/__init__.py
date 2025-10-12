"""
Finam Trade API Client Module

This module provides a Python interface to the Finam Trade API,
with functionality for market data retrieval and trading inefficiency monitoring.
"""

from .client import FinamClient
from .market_data import MarketDataService
from .monitor import DivergenceMonitor

__version__ = "0.1.0"

__all__ = [
    "FinamClient",
    "MarketDataService",
    "DivergenceMonitor",
]
