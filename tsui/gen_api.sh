java -jar openapi-generator-cli-3.2.1.jar generate -i http://localhost:8080/v2/api-docs -g typescript-fetch -o ./tmp
mv ./tmp/* ./src/api
