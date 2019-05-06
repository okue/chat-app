.PHONY: server client
STATIC_DIR=./server/src/main/resources/static/

build: client server 
	docker-compose build

run:
	docker-compose up

stop:
	docker-compose down

client:
	make -C client build
	cp client/index.html   ${STATIC_DIR}
	cp client/main_elm.js  ${STATIC_DIR}
	cp client/main_grpc.js ${STATIC_DIR}

server:
	cd server/ && ./gradlew build
