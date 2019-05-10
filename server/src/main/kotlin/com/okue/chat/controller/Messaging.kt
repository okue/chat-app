package com.okue.chat.controller

import com.okue.controller.MessagingGrpc
import com.okue.controller.Msg
import com.okue.controller.ReceiveMsgsReply
import com.okue.controller.ReceiveMsgsRequest
import com.okue.controller.Result
import com.okue.controller.SendMsgReply
import com.okue.controller.SendMsgRequest
import com.okue.controller.User
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.lognet.springboot.grpc.GRpcService
import org.springframework.beans.factory.annotation.Value
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.util.logging.Logger

// NOTE
// https://github.com/LogNet/grpc-spring-boot-starter

// NOTE for grpcc
// client.receiveMsgs({user : { name : "fuga" }}).on('data', sr).on('status', sr)
// client.sendMsg({ user: { name: "fuga" }, msg: { from: { name : "fuga" }, to: { name : "aaa" }, content: "sakana_wa_yaku" } }, pr)

@GRpcService
class Messaging : MessagingGrpc.MessagingImplBase() {

    @Value("\${redis.host}")
    lateinit var REDIS_HOST: String

    override fun sendMsg(request: SendMsgRequest, responseObserver: StreamObserver<SendMsgReply>) {
        logger.info("${request.msg} by ${Thread.currentThread().name}")

        val jedis = Jedis(REDIS_HOST)
        val r = Result
            .newBuilder()
            .setStatus(Result.Status.Ok)
        val reply = SendMsgReply
            .newBuilder()
            .setResult(r)

        runBlocking(Dispatchers.IO) {
            this.launch {
                jedis.publish(request.msg.to.name.toByteArray(), request.msg.toByteArray())
                logger.info("${this.coroutineContext.toString()}")
                logger.info("${request.msg} published by ${Thread.currentThread().name}")
            }
            responseObserver.onNext(reply.build())
            responseObserver.onCompleted()
        }
    }

    override fun receiveMsgs(request: ReceiveMsgsRequest, responseObserver: StreamObserver<ReceiveMsgsReply>) {
        val jedis = Jedis(REDIS_HOST)
        logger.info("user ${request.user.name} by ${Thread.currentThread().id}")

        val job = runBlocking(Dispatchers.IO) {
            this.launch {
                logger.info("== start to subscribe ==")
                jedis.subscribe(Subscriber(responseObserver), request.user.name)
                logger.info("== stop to subscribe ==")
            }
        }

        (responseObserver as ServerCallStreamObserver).setOnCancelHandler {
            logger.info("== job cancel ==")
            job.cancel()
        }
    }

    inner class Subscriber(observer: StreamObserver<ReceiveMsgsReply>) : JedisPubSub() {

        val obs = observer

        override fun onMessage(channel: String, message: String) {
            logger.info("${Thread.currentThread().name} gets ${message} on ${channel}")
            val reply =
                try {
                    ReceiveMsgsReply
                        .newBuilder()
                        .setMsg(Msg.parseFrom(message.toByteArray()))
                } catch (e: Exception) {
                    ReceiveMsgsReply
                        .newBuilder()
                        .setMsg(
                            Msg.newBuilder()
                                .setTo(User.newBuilder().setName("admin"))
                                .setFrom(User.newBuilder().setName("admin"))
                                .setContent("hoge is hoge")
                        )
                }
            obs.onNext(reply.build())
        }
    }

    companion object {
        val logger = Logger.getLogger(this::class.java.name)
    }
}
