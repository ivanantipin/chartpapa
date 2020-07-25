if [[ $# -eq 0 ]] ; then
    echo 'you need to specify environment'
    exit 1
fi

env=$1
version=20200725.1

docker pull ivanantipin/chartpapa:${version}

containerName=AllModels-${env}

docker rm -f ${containerName}

docker run -e env=$env --name ${containerName} --network host -d -v /ddisk/globaldatabase:/ddisk/globaldatabase ivanantipin/chartpapa:${version}