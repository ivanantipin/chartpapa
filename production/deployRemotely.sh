../gradlew clean build installDist

export home_dir=release/volabreak
export host=ivan@192.168.0.10

ssh $host "mkdir -v -p ~/${home_dir}"

rm -rf ./production-0.0.1-SNAPSHOT

unzip ./build/distributions/production-0.0.1-SNAPSHOT.zip

scp -r ./production-0.0.1-SNAPSHOT ${host}:${home_dir}

rm -rf ./production-0.0.1-SNAPSHOT