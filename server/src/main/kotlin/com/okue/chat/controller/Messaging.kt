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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lognet.springboot.grpc.GRpcService
import org.springframework.beans.factory.annotation.Value
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.util.logging.Logger

// NOTE for grpcc
// client.receiveMsgs({user : { name : "fuga" }}).on('data', sr).on('status', sr)
// client.sendMsg({ user: { name: "fuga" }, msg: { from: { name : "fuga" }, to: { name : "aaa" }, content: "sakana_wa_yaku" } }, pr)

@GRpcService
class Messaging : MessagingGrpc.MessagingImplBase() {

    @Value("\${redis.host}")
    lateinit var REDIS_HOST: String

    override fun sendMsg(request: SendMsgRequest, responseObserver: StreamObserver<SendMsgReply>) {
        val jedis = Jedis(REDIS_HOST)
        val r = Result
            .newBuilder()
            .setStatus(Result.Status.Ok)
        val reply = SendMsgReply
            .newBuilder()
            .setResult(r)

        CoroutineScope(Dispatchers.IO).launch {
            jedis.publish(request.msg.to.name.toByteArray(), request.msg.toByteArray())
            logger.info(
                "from ${request.msg.from.name} "
                        + "to ${request.msg.to.name} "
                        + "content ${request.msg.content} "
                        + "published by ${Thread.currentThread().name}"
            )
        }
        responseObserver.onNext(reply.build())
        responseObserver.onCompleted()
    }

    override fun receiveMsgs(request: ReceiveMsgsRequest, responseObserver: StreamObserver<ReceiveMsgsReply>) {
        logger.info("user ${request.user.name} by ${Thread.currentThread().id}")
        val jedis = Jedis(REDIS_HOST)
        val subscriber = Subscriber(responseObserver)

        CoroutineScope(Dispatchers.IO).launch {
            logger.info("== start to subscribe ==")
            jedis.subscribe(subscriber, request.user.name)
            logger.info("== stop to subscribe ==")
        }

        (responseObserver as ServerCallStreamObserver).setOnCancelHandler {
            subscriber.unsubscribe()
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
