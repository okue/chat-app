package com.okue.chat.controller

import com.okue.chat.Observers
import com.okue.chat.Subscriber
import com.okue.controller.MessagingGrpc
import com.okue.controller.ReceiveMsgsReply
import com.okue.controller.ReceiveMsgsRequest
import com.okue.controller.Result
import com.okue.controller.SendMsgReply
import com.okue.controller.SendMsgRequest
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.lognet.springboot.grpc.GRpcService
import org.springframework.beans.factory.annotation.Value
import redis.clients.jedis.Jedis
import java.util.logging.Logger

// NOTE for grpcc
// client.receiveMsgs({user : { name : "fuga" }}).on('data', sr).on('status', sr)
// client.sendMsg({ user: { name: "fuga" }, msg: { from: { name : "fuga" }, to: { name : "aaa" }, content: "sakana_wa_yaku" } }, pr)

@GRpcService
class Messaging : MessagingGrpc.MessagingImplBase() {

    private var logger = Logger.getLogger(this::class.java.name)

    @Value("\${redis.host}")
    lateinit var REDIS_HOST: String

    init {
        GlobalScope.launch {
            val jedis = Jedis(REDIS_HOST)
            logger.info("[${Thread.currentThread().name}] start to subscribe")
            jedis.psubscribe(Subscriber, "client.*")
            logger.info("[${Thread.currentThread().name}] stop to subscribe")
        }
    }

    private fun toChannelName(userName: String): ByteArray {
        return ("client." + userName).toByteArray()
    }

    override fun sendMsg(request: SendMsgRequest, responseObserver: StreamObserver<SendMsgReply>) {
        val jedis = Jedis(REDIS_HOST)
        val r = Result
            .newBuilder()
            .setStatus(Result.Status.Ok)
        val reply = SendMsgReply
            .newBuilder()
            .setResult(r)
        val channelName = request.msg.to.name.let { toChannelName(it) }

        CoroutineScope(Dispatchers.IO).launch {
            jedis.publish(channelName, request.msg.toByteArray())
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
        Observers.put(request.user.name, responseObserver)
    }

}
