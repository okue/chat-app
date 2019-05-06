include ../.env
OUTPUT=./
JS=${OUTPUT}/${ELMJS}
OPT=make --optimize Main.elm --output=${JS}
# CSS=assets/style.css

build: compile genProto grpcJs

compile:
	elm ${OPT}

reactor:
	elm reactor

COMMONJS_DIR=./js
genProto:
	protoc -I=../server/src/main/proto/ messaging.proto\
		--js_out=import_style=commonjs:${COMMONJS_DIR}\
		--grpc-web_out=import_style=commonjs,mode=grpcwebtext:${COMMONJS_DIR}

grpcJs:
	make -C ./js compile
