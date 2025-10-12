"""
Finam Trade API Client - gRPC-based using official finam-trade-api package
"""

import logging
from typing import Optional
from finam_trade_api import Client as FinamAPIClient
from finam_trade_api import TokenManager

from .config import FINAM_API_TOKEN, validate_config

logger = logging.getLogger(__name__)


class FinamClient:
    """
    Wrapper for the official Finam Trade API client

    Provides a simplified interface to the async gRPC-based API.
    Uses the official finam-trade-api package.
    """

    def __init__(self, api_token: Optional[str] = None):
        """
        Initialize Finam API client

        Args:
            api_token: API token (if not provided, uses FINAM_API_TOKEN from config)
        """
        validate_config()

        self.api_token = api_token or FINAM_API_TOKEN
        self.token_manager = TokenManager(self.api_token)
        self.client = FinamAPIClient(self.token_manager)

        logger.info("Finam gRPC client initialized")

    async def authenticate(self) -> None:
        """
        Authenticate with Finam API and set JWT token
        """
        await self.client.access_tokens.set_jwt_token()
        logger.info("Successfully authenticated with Finam API")

    async def get_account_ids(self) -> list[str]:
        """
        Get list of account IDs available for this token

        Returns:
            List of account IDs that can be used with the API
        """
        token_details = await self.client.access_tokens.get_jwt_token_details()
        account_ids = token_details.account_ids
        logger.info(f"Retrieved {len(account_ids)} account IDs")
        return list(account_ids)

    async def close(self) -> None:
        """Close the client session"""
        # The official client handles cleanup internally
        logger.info("Finam client session closed")

    async def __aenter__(self):
        """Async context manager entry"""
        await self.authenticate()
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """Async context manager exit"""
        await self.close()

    @property
    def instruments(self):
        """Access to instruments/market data methods"""
        return self.client.instruments

    @property
    def account(self):
        """Access to account methods"""
        return self.client.account

    @property
    def assets(self):
        """Access to assets methods"""
        return self.client.assets

    @property
    def orders(self):
        """Access to orders methods"""
        return self.client.orders
