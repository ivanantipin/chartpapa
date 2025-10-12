"""Test the official finam-trade-api package"""
import asyncio
import os
from dotenv import load_dotenv
from finam_trade_api import Client
from finam_trade_api import TokenManager

load_dotenv('finam_trader/.env')

async def main():
    token = os.getenv('FINAM_API_TOKEN')
    print(f"Token (first 50 chars): {token[:50]}...")

    # Create client
    client = Client(TokenManager(token))

    # Set JWT token
    await client.access_tokens.set_jwt_token()
    print("[OK] JWT token set successfully")

    # Try to get list of instruments (this seems to be the correct method)
    try:
        print("\n=== Trying to get instruments list ===")
        print(f"Client instruments attributes: {[x for x in dir(client.instruments) if not x.startswith('_')]}")
    except Exception as e:
        print(f"[ERROR] {e}")

    # Let's try with account info first
    try:
        print("\n=== Getting account info ===")
        accounts = await client.account.get_accounts()
        print(f"[OK] Retrieved {len(accounts.accounts)} accounts")
        if accounts.accounts:
            account_id = accounts.accounts[0].account_id
            print(f"Using account: {account_id}")

            # Try getting an instrument with account_id
            print("\n=== Trying to get quote ===")
            quote_response = await client.instruments.get_last_quote(
                symbol="SBER",  # Try without @TQBR
                account_id=account_id
            )
            print(f"[OK] Got quote")
            print(f"  Last: {quote_response.last}")
            print(f"  Bid: {quote_response.bid}")
            print(f"  Ask: {quote_response.ask}")
    except Exception as e:
        print(f"[ERROR] {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    asyncio.run(main())
