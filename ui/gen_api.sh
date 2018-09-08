java -jar openapi-generator-cli-3.2.1.jar generate -i http://localhost:8080/v2/api-docs -g javascript-flowtyped -o /tmp/api
mv /tmp/api/src/* ./src/api
