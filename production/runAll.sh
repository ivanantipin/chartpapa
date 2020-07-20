if [[ $# -eq 0 ]] ; then
    echo 'you need to specify environment'
    exit 1
fi

env=$1
version=20200719-dockerizing

docker pull ivanantipin/chartpapa:${version}

docker run -e env=$env --name Reconnect-${env} --network host -d -v /ddisk/globaldatabase:/ddisk/globaldatabase ivanantipin/chartpapa:${version} reconnect

docker run -e env=$env --name AllModels-${env} --network host -d -v /ddisk/globaldatabase:/ddisk/globaldatabase ivanantipin/chartpapa:${version} RealDivModel TrendModel