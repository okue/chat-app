package com.okue.chat.controller

import com.okue.controller.MessagingGrpc
import com.okue.controller.Msg
import com.okue.controller.ReceiveMsgsReply
import com.okue.controller.ReceiveMsgsRequest
import com.okue.controller.Result
import com.okue.controller.SendMsgReply
import com.okue.controller.SendMsgRequest
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.lognet.springboot.grpc.GRpcService
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub

// NOTE
// https://github.com/LogNet/grpc-spring-boot-starter

// NOTE for grpcc
// client.receiveMsgs({user : { name : "fuga" }}).on('data', sr).on('status', sr)
// client.sendMsg({ user: { name: "fuga" }, msg: { from: { name : "fuga" }, to: { name : "aaa" }, content: "sakana_wa_yaku" } }, pr)

@GRpcService
class Messaging : MessagingGrpc.MessagingImplBase() {

    val BROADCAST = "broadcast"
    val REDIS_HOST = "redis_dev"

    override fun sendMsg(request: SendMsgRequest, responseObserver: StreamObserver<SendMsgReply>) {
        println("${request.msg}")

        val jedis = Jedis(REDIS_HOST)
        val r = Result
            .newBuilder()
            .setStatus(Result.Status.Ok)
            .setReason("")
            .build()
        val reply = SendMsgReply
            .newBuilder()
            .setResult(r)
            .build()
        GlobalScope.launch {
            jedis.publish(BROADCAST.toByteArray(), request.msg.toByteArray())
        }
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
    }

    override fun receiveMsgs(request: ReceiveMsgsRequest, responseObserver: StreamObserver<ReceiveMsgsReply>) {
        val jedis = Jedis(REDIS_HOST)
        val user = request.user
        println("user ${user.name}")

        val ch = Channel<ByteArray>()

        GlobalScope.async {
            jedis.subscribe(Subscriber(ch), BROADCAST)
        }

        GlobalScope.launch {
            for (m in ch) {
                println("get message")

                val reply = ReceiveMsgsReply
                    .newBuilder()
                    .setMsg(Msg.parseFrom(m))
                    .build()

                responseObserver.onNext(reply)
            }
        }
        // responseObserver.onCompleted()
    }

    inner class Subscriber(val ch : Channel<ByteArray>) : JedisPubSub() {
        override fun onMessage(channel: String, message: String) {
            println(this)
            GlobalScope.launch {
                ch.send(message.toByteArray())
            }
        }
    }
}
