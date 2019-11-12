./gradlew clean build installDist

scp -r grpc-back/build/install/grpc-back root@95.216.162.4:

ssh root@95.216.162.4 'ps -ef | grep java | grep -v grep |  while read f f1 junk; do kill ${f1}; done;'

ssh root@95.216.162.4 'nohup ~/grpc-back/bin/startServer > ~/server.log 2>&1 &'

ssh root@95.216.162.4 "tail ~/server.log"
