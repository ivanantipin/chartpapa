../gradlew clean build installDist


if [[ $# -eq 0 ]] ; then
    echo 'no arguments supplied, need to provide tag'
    exit 1
fi

tag=$1

echo "tag to be deployed $1"

export host=root@95.216.162.4

ssh $host "export TAG=${tag}; docker-compose pull"
ssh $host "docker docker-compose down"
ssh $host "export TAG=${tag}; docker-compose up"