rm -rf ./stockvizclient/src
rm -rf ./stockvizclient/docs
rm -rf ./stockvizclient/settings.gradle
java -jar openapi-generator-cli.jar generate -i http://localhost:8000/swagger.json -g kotlin -o ./stockvizclient -c genconfig.json