
echo $PWD
PATH=PATH:/home/ivan/.pub-cache/bin:/snap/bin
protoc -I grpc-back/src/main/proto services.proto --dart_out=grpc:mobile_app/lib/gen
