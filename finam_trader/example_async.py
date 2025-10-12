"""
Async example for Finam Trade API client (gRPC)
"""

import asyncio
import logging
from finam_trader import FinamClient, MarketDataService

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

async def main():
    # Initialize client with async context manager
    async with FinamClient() as client:
        # Get available account IDs
        print("\n=== Getting available accounts ===")
        try:
            account_ids = await client.get_account_ids()
            print(f"  Found {len(account_ids)} account(s)")
            for i, acc_id in enumerate(account_ids, 1):
                print(f"  {i}. {acc_id}")

            # Use the first account
            if not account_ids:
                print("  No accounts found!")
                return
            account_id = account_ids[0]
            print(f"  Using account: {account_id}")
        except Exception as e:
            print(f"  Error: {e}")
            return

        # Get account info
        print("\n=== Getting account info ===")
        try:
            account_info = await client.account.get_account_info(account_id)
            print(f"  Account ID: {account_info.account_id}")
            print(f"  Type: {account_info.type}")
            print(f"  Status: {account_info.status}")
        except Exception as e:
            print(f"  Error: {e}")

        # Create market data service
        market_data = MarketDataService(client)

        # Example 1: Get quote for specific instrument
        # Note: Symbol format should include exchange code (e.g., "SBER@MISX")
        print("\n=== Getting quote ===")
        try:
            quote = await market_data.get_quote("SBER@MISX")
            print(f"  Symbol: {quote['symbol']}")
            print(f"  Last price: {quote['last']}")
            print(f"  Bid: {quote['bid']}")
            print(f"  Ask: {quote['ask']}")
            print(f"  Volume: {quote['volume']}")
        except Exception as e:
            print(f"  Error: {e}")

        # Example 2: Compare base asset with futures
        # Note: You need to find the actual futures contract symbol
        # Futures contracts expire and have specific ticker formats
        # TODO: Replace with actual futures contract symbol once you know it
        print("\n=== Comparing base asset with futures ===")
        print("  Skipped: Futures symbol needs to be determined")
        print("  Note: Replace 'FUTURES_SYMBOL@MISX' with actual contract")
        # Example code (uncomment when you have the right symbol):
        # try:
        #     comparison = await market_data.get_price_comparison(
        #         "SBER@MISX",           # Base asset on stock exchange
        #         "FUTURES_SYMBOL@MISX"  # Replace with actual futures contract
        #     )
        #     print(f"  Base price: {comparison['base_price']}")
        #     print(f"  Futures price: {comparison['futures_price']}")
        #     print(f"  Spread: {comparison['spread_percent']:.2f}%")
        # except Exception as e:
        #     print(f"  Error: {e}")

if __name__ == "__main__":
    asyncio.run(main())
