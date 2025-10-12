# Finam Trade API Client (gRPC)

Python module for connecting to Finam Trade API and monitoring trading inefficiencies, particularly divergences between base assets and futures.

## Features

- **gRPC-based**: Uses official `finam-trade-api` package
- **Async/Await**: Modern async Python for efficient I/O
- **Market Data**: Real-time quotes, order books, historical bars, and trades
- **Divergence Monitoring**: Automated monitoring of price spreads between base assets and futures
- **Alert System**: Configurable callbacks for divergence alerts
- **Account Management**: Access to account information and trading

## Installation

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Configure your API token in `.env`:
```bash
# .env file
FINAM_API_TOKEN=your_token_here
```

## Quick Start

### Basic Usage

```python
import asyncio
from finam_trader import FinamClient, MarketDataService

async def main():
    # Initialize client with async context manager
    async with FinamClient() as client:
        # Get available account IDs
        account_ids = await client.get_account_ids()
        account_id = account_ids[0]  # Use first account

        # Get account information
        account_info = await client.account.get_account_info(account_id)
        print(f"Account: {account_info.account_id}, Status: {account_info.status}")

        market_data = MarketDataService(client)

        # Get quote for an instrument (note: symbol includes exchange code)
        quote = await market_data.get_quote("SBER@MISX")
        print(f"Last price: {quote['last']}")

        # Compare base asset with futures
        comparison = await market_data.get_price_comparison(
            "SBER@MISX",          # Base asset on stock exchange
            "FUTURES_SYMBOL@MISX" # Futures contract (use actual symbol)
        )
        print(f"Spread: {comparison['spread_percent']:.2f}%")

asyncio.run(main())
```

### Monitoring Divergences

The `monitor.py` module provides divergence detection:

```python
import asyncio
from datetime import datetime
from finam_trader import FinamClient, MarketDataService
from finam_trader.monitor import DivergenceAlert

async def monitor_divergence():
    async with FinamClient() as client:
        market_data = MarketDataService(client)

        # Get prices
        comparison = await market_data.get_price_comparison("SBER", "SRH5")

        spread_percent = comparison['spread_percent']

        # Create alert if threshold exceeded
        if abs(spread_percent) >= 1.0:  # 1% threshold
            alert = DivergenceAlert(
                timestamp=datetime.now().isoformat(),
                base_symbol="SBER",
                futures_symbol="SRH5",
                base_price=comparison['base_price'],
                futures_price=comparison['futures_price'],
                spread=comparison['spread'],
                spread_percent=spread_percent,
                alert_type="futures_premium" if spread_percent > 0 else "futures_discount",
                severity="high" if abs(spread_percent) >= 2.0 else "medium"
            )
            print(alert.to_json())

asyncio.run(monitor_divergence())
```

## Module Structure

```
finam_trader/
├── __init__.py           # Main module exports
├── client.py             # Async gRPC client wrapper
├── market_data.py        # Market data service (async)
├── monitor.py            # Divergence monitoring and alerts
├── config.py             # Configuration management
├── requirements.txt      # Python dependencies
├── .env                  # Your API token (not in git)
├── example_async.py      # Async usage examples
└── README.md             # This file
```

## API Reference

### FinamClient

Main async client for API communication.

**Methods:**
- `async authenticate()` - Authenticate with Finam API
- `async get_account_ids()` - Get list of available account IDs
- `async close()` - Close session
- Properties: `instruments`, `account`, `assets`, `orders`

**Usage:**
```python
async with FinamClient() as client:
    # Client is authenticated and ready
    account_ids = await client.get_account_ids()
    quote = await client.instruments.get_last_quote(symbol="SBER@MISX")
```

### MarketDataService

Async service for retrieving market data.

**Methods:**
- `async get_quote(symbol)` - Get current quote
- `async get_order_book(symbol)` - Get order book
- `async get_bars(symbol, timeframe, from_time, to_time, count)` - Get historical bars
- `async get_latest_trades(symbol)` - Get recent trades
- `async get_price_comparison(base_symbol, futures_symbol)` - Compare prices

**Timeframes:** `"1m"`, `"5m"`, `"15m"`, `"30m"`, `"1h"`, `"4h"`, `"1d"`, `"1w"`, `"1M"`

### DivergenceMonitor

Monitor for detecting price divergences (see `monitor.py`).

**Key Classes:**
- `DivergenceAlert` - Data class for divergence alerts
- Alert severity levels: `medium`, `high`, `critical`
- Alert types: `futures_premium`, `futures_discount`

## Configuration

Configure via `.env` file:

| Variable | Description | Default |
|----------|-------------|---------|
| `FINAM_API_TOKEN` | Your API token (required) | - |
| `FINAM_API_URL` | gRPC API URL | `https://tradeapi.finam.ru` |
| `DIVERGENCE_THRESHOLD_PERCENT` | Divergence alert threshold | `1.0` |
| `MONITORING_INTERVAL_SECONDS` | Monitoring check interval | `60` |
| `REQUEST_TIMEOUT` | Request timeout in seconds | `30` |

## Symbol Format

**IMPORTANT**: Symbols MUST include the exchange code to work correctly:

The format is: `SYMBOL@EXCHANGE_CODE`

Examples:
- Stock: `"SBER@MISX"` (Sberbank on MOEX)
- Currency: `"USDRUB_TOM@CETS"`
- Futures: Contact varies - you'll need to determine the specific ticker

Common exchange codes:
- `MISX` - Moscow Exchange (used in API examples)
- `TQBR` - MOEX stock market (T+ sector)
- `RFUD` - MOEX futures
- `CETS` - MOEX currency market

**Note**: The exact symbol format may vary. Use `@MISX` as shown in the official API examples.

## Error Handling

The module includes comprehensive error handling:
- All API errors are logged and re-raised
- Use try/except blocks for specific error handling
- Check logs for detailed error information

## Examples

See `example_async.py` for complete async usage examples including:
- Authentication
- Getting account information
- Retrieving quotes
- Price comparison
- Error handling

## Technical Details

- **API Type**: gRPC-based via official `finam-trade-api` package
- **Python Version**: 3.11+
- **Async**: Uses asyncio for concurrent operations
- **Dependencies**: `finam-trade-api`, `python-dotenv`

## Resources

- **API Documentation**: https://tradeapi.finam.ru/docs/
- **Official Package**: https://pypi.org/project/finam-trade-api/
- **GitHub Repo**: https://github.com/FinamWeb/finam-trade-api/
- **Support**: trade_api@corp.finam.ru

## License

This is a client library for Finam Trade API. Refer to Finam's terms of service for API usage.
