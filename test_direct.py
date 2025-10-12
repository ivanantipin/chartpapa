"""Direct API test to understand the correct format"""
import requests
import os
from dotenv import load_dotenv

load_dotenv('finam_trader/.env')

token = os.getenv('FINAM_API_TOKEN')
print(f"Token (first 50 chars): {token[:50]}...")

# Try different endpoint formats
base_urls = [
    "https://tradeapi.finam.ru",
]

endpoints = [
    "/v1/assets",
    "/tradeapi.v1.assets.AssetsService/Assets",
]

headers = {
    "Authorization": f"Bearer {token}",
    "User-Agent": "ChartPapa/1.0",
    "Content-Type": "application/json"
}

for base in base_urls:
    for endpoint in endpoints:
        url = f"{base}{endpoint}"
        print(f"\nTrying: {url}")
        try:
            # Try POST with empty body
            resp = requests.post(url, json={}, headers=headers, timeout=5)
            print(f"  POST: {resp.status_code}")
            if resp.status_code == 200:
                print(f"  SUCCESS! Response: {resp.json()}")
        except Exception as e:
            print(f"  POST Error: {e}")

        try:
            # Try GET
            resp = requests.get(url, headers=headers, timeout=5)
            print(f"  GET: {resp.status_code}")
            if resp.status_code == 200:
                print(f"  SUCCESS! Response: {resp.json()}")
        except Exception as e:
            print(f"  GET Error: {e}")
