echo $PWD
PATH=PATH:/home/ivan/.pub-cache/bin:/snap/bin
cd /home/ivan/projects/chartpapa/
protoc -I grpc-back/src/main/proto alfa.proto --dart_out=grpc:mobile_app/lib/gen
