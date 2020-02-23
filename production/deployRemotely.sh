../gradlew clean build installDist

export home_dir=release/prod0
export host=ivan@192.168.0.10

ssh $host "mkdir -v -p ~/${home_dir}"

scp -r build/install/production ${host}:${home_dir}

scp -rv ../market_research ${host}:${home_dir}