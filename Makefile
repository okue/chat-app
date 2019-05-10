.PHONY: server client
include .env
STATIC_DIR=./server/src/main/resources/static/

build: client server 
	docker-compose build

up: run
run:
	docker-compose up

down: stop
stop:
	docker-compose down

client:
	make -C client build
	cp client/index.html   ${STATIC_DIR}
	cp client/${ELMJS}  ${STATIC_DIR}
	cp client/${MAINJS} ${STATIC_DIR}

server:
	cd server/ && ./gradlew bootJar
