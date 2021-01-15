rm -rf ./stockvisback/src
rm -rf ./stockvisback/docs
rm -rf ./stockvisback/settings.gradle
./gen.sh generate -i http://localhost:8000/swagger.json -g kotlin-spring --additional-properties=serviceInterface=true -o ./stockvisback -c genconfig.json

