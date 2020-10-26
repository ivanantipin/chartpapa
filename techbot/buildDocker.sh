../gradlew clean build installDist

version=`git tag --points-at HEAD | head -n 1`

echo "version is ${version}"

docker build  -t ivanantipin/techbot:$version .

docker push ivanantipin/techbot:$version