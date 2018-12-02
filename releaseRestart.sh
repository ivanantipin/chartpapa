ps -e | grep java | while read r junk; do kill $r; done;
git pull
echo "latest logs"
echo "======================"
tail -n 100 nohup
echo "========================="
rm nohup
nohup ./gradlew bootRun &