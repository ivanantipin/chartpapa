rm -fr ./src/api
./gen.sh generate -i http://localhost:8080/swagger/demo-0.0.yml -g typescript-fetch -o ./src/api
