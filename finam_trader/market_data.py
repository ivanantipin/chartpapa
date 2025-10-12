"""
Market Data Service - Retrieves market data for instruments via gRPC
"""

import logging
from typing import Dict, Any, List, Optional
from datetime import datetime

from .client import FinamClient

logger = logging.getLogger(__name__)


class MarketDataService:
    """
    Service for retrieving market data from Finam Trade API (gRPC)

    Provides async methods for:
    - Getting current quotes
    - Retrieving order book data
    - Fetching historical bars
    - Getting latest trades
    """

    def __init__(self, client: FinamClient):
        """
        Initialize market data service

        Args:
            client: Authenticated FinamClient instance
        """
        self.client = client

    async def get_quote(self, symbol: str) -> Dict[str, Any]:
        """
        Get current quote for an instrument

        Args:
            symbol: Instrument symbol (e.g., "SBER@MISX", "SBER@TQBR")

        Returns:
            Quote data including bid, ask, last price, volume, etc.
        """
        try:
            quote_response = await self.client.instruments.get_last_quote(symbol=symbol)
            quote = quote_response.quote
            logger.debug(f"Retrieved quote for {symbol}: {quote.last}")

            # Convert to dict for compatibility
            # Note: Decimal values have a .value attribute
            return {
                "symbol": symbol,
                "last": float(quote.last.value) if quote.last else 0.0,
                "bid": float(quote.bid.value) if quote.bid else 0.0,
                "ask": float(quote.ask.value) if quote.ask else 0.0,
                "volume": float(quote.volume.value) if quote.volume else 0.0,
                "timestamp": datetime.now().isoformat()
            }
        except Exception as e:
            logger.error(f"Failed to get quote for {symbol}: {e}")
            raise

    async def get_order_book(self, symbol: str) -> Dict[str, Any]:
        """
        Get order book (market depth) for an instrument

        Args:
            symbol: Instrument symbol

        Returns:
            Order book with buy and sell levels
        """
        try:
            order_book = await self.client.instruments.get_order_book(symbol=symbol)
            logger.debug(f"Retrieved order book for {symbol}")

            return {
                "symbol": symbol,
                "bids": order_book.bids,
                "asks": order_book.asks,
                "timestamp": datetime.now().isoformat()
            }
        except Exception as e:
            logger.error(f"Failed to get order book for {symbol}: {e}")
            raise

    async def get_bars(
        self,
        symbol: str,
        timeframe: str,
        from_time: datetime,
        to_time: Optional[datetime] = None,
        count: Optional[int] = None
    ) -> List[Dict[str, Any]]:
        """
        Get historical price bars for an instrument

        Args:
            symbol: Instrument symbol
            timeframe: Timeframe (e.g., "1m", "5m", "1h", "1d")
            from_time: Start time for historical data
            to_time: End time (optional, defaults to now)
            count: Maximum number of bars to retrieve (optional)

        Returns:
            List of price bars with OHLCV data
        """
        if to_time is None:
            to_time = datetime.now()

        try:
            # Import BarsRequest model
            from finam_trade_api.instruments.model import BarsRequest

            bars_request = BarsRequest(
                symbol=symbol,
                timeframe=timeframe,
                from_time=from_time,
                to_time=to_time,
                count=count
            )

            bars_response = await self.client.instruments.get_bars(params=bars_request)

            bars = []
            for bar in bars_response.bars:
                bars.append({
                    "timestamp": bar.timestamp,
                    "open": bar.open,
                    "high": bar.high,
                    "low": bar.low,
                    "close": bar.close,
                    "volume": bar.volume
                })

            logger.debug(f"Retrieved {len(bars)} bars for {symbol}")
            return bars
        except Exception as e:
            logger.error(f"Failed to get bars for {symbol}: {e}")
            raise

    async def get_latest_trades(self, symbol: str) -> List[Dict[str, Any]]:
        """
        Get latest trades for an instrument

        Args:
            symbol: Instrument symbol

        Returns:
            List of recent trades
        """
        try:
            trades_response = await self.client.instruments.get_last_trades(symbol=symbol)

            trades = []
            for trade in trades_response.trades:
                trades.append({
                    "timestamp": trade.timestamp,
                    "price": trade.price,
                    "quantity": trade.quantity,
                    "side": trade.side
                })

            logger.debug(f"Retrieved {len(trades)} trades for {symbol}")
            return trades
        except Exception as e:
            logger.error(f"Failed to get latest trades for {symbol}: {e}")
            raise

    async def get_price_comparison(
        self,
        base_symbol: str,
        futures_symbol: str
    ) -> Dict[str, Any]:
        """
        Get price comparison between base asset and futures

        Args:
            base_symbol: Symbol of base asset
            futures_symbol: Symbol of futures contract

        Returns:
            Dictionary with both quotes and calculated spread
        """
        try:
            base_quote = await self.get_quote(base_symbol)
            futures_quote = await self.get_quote(futures_symbol)

            base_price = base_quote.get("last")
            futures_price = futures_quote.get("last")

            if base_price and futures_price:
                spread = futures_price - base_price
                spread_percent = (spread / base_price) * 100 if base_price != 0 else 0
            else:
                spread = None
                spread_percent = None

            result = {
                "base_symbol": base_symbol,
                "futures_symbol": futures_symbol,
                "base_quote": base_quote,
                "futures_quote": futures_quote,
                "base_price": base_price,
                "futures_price": futures_price,
                "spread": spread,
                "spread_percent": spread_percent,
                "timestamp": datetime.now().isoformat()
            }

            logger.info(
                f"Price comparison: {base_symbol}={base_price}, "
                f"{futures_symbol}={futures_price}, spread={spread_percent:.2f}%"
            )

            return result

        except Exception as e:
            logger.error(f"Failed to get price comparison: {e}")
            raise
