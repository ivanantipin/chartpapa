../gradlew clean build installDist

version=`git tag --points-at HEAD | head -n 1`

docker build  -t ivanantipin/chartpapa:$version .

docker push ivanantipin/chartpapa:$version