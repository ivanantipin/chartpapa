java -jar openapi-generator-cli.jar generate -i http://localhost:8000/swagger.json -g kotlin -o ./tmp -c genconfig.json

rm -rf ./stockvizclient/*
mkdir -p ./stockvizclient
mv ./tmp/* ./stockvizclient