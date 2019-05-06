OUTPUT=./
JS=${OUTPUT}/main_elm.js
CSS=assets/style.css
OPT=make --optimize Main.elm --output=${JS}

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
