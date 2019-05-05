
OUTPUT=./output

protoc -I=../server/src/main/proto/ messaging.proto --js_out=import_style=commonjs:$OUTPUT --grpc-web_out=import_style=commonjs,mode=grpcwebtext:$OUTPUT
