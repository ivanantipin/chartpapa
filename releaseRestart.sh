#!/usr/bin/env bash
ps -e | grep java | grep -v -i idea | while read r junk; do kill $r; done;
git pull
echo "latest logs"
echo "======================"
tail -n 100 nohup.out
echo "========================="

./gradlew clean build installDist

nohup ./grpc-back/build/install/grpc-back/bin/startServer &

sleep 10

tail -n 100 nohup.out
