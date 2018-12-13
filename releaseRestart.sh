#!/usr/bin/env bash
ps -e | grep java | while read r junk; do kill $r; done;
git pull
echo "latest logs"
echo "======================"
tail -n 100 nohup.out
echo "========================="
rm nohup.out
nohup ./gradlew bootRun &

