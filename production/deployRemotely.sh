../gradlew clean build installDist


if [[ $# -eq 0 ]] ; then
    echo 'no arguments supplied, need to provid model name'
    exit 1
fi

modelName=$1

echo "model to be deployed $1"

export home_dir="release/$modelName"
export host=ivan@192.168.0.10

ssh $host "mkdir -v -p ~/${home_dir}"

rm -rf ./production-0.0.1-SNAPSHOT

unzip ./build/distributions/production-0.0.1-SNAPSHOT.zip

scp -r ./production-0.0.1-SNAPSHOT ${host}:${home_dir}

rm -rf ./production-0.0.1-SNAPSHOT