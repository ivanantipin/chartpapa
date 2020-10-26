version=20200924-tickers-in-db

docker pull ivanantipin/techbot:${version}

containerName=TechBot

docker rm -f ${containerName}

docker run -e env=$env --restart=unless-stopped --name ${containerName} --network host -d ivanantipin/techbot:${version}