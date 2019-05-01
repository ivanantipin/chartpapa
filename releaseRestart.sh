#!/usr/bin/env bash
ps -e | grep java | grep -v -i idea | while read r junk; do kill $r; done;
git pull
echo "latest logs"
echo "======================"
tail -n 100 nohup.out
echo "========================="

cd tsui && npm install && npm run build

cd ..

cp -r tsui/build/* back/src/main/resources/static/

rm nohup.out

args="-Xms728m -Xmx728m -DnotebooksDir=/root/release/chartpapa/market_research/published \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=5555 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Dserver.port=80 \
-Dcom.sun.management.jmxremote.local.only=false"

nohup ./gradlew --stacktrace bootRun -PjvmArgs="${args}" &