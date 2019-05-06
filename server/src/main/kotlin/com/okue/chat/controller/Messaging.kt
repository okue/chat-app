package com.okue.chat.controller

import com.okue.controller.MessagingGrpc
import com.okue.controller.Msg
import com.okue.controller.ReceiveMsgsReply
import com.okue.controller.ReceiveMsgsRequest
import com.okue.controller.Result
import com.okue.controller.SendMsgReply
import com.okue.controller.SendMsgRequest
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    // val REDIS_HOST = "redis_dev"
    val REDIS_HOST = "0.0.0.0"

    override fun sendMsg(request: SendMsgRequest, responseObserver: StreamObserver<SendMsgReply>) {
        println("${request.msg} by ${Thread.currentThread().id}")

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
        runBlocking {
            async {
                jedis.publish(BROADCAST.toByteArray(), request.msg.toByteArray())
            }.await()
            responseObserver.onNext(reply)
            responseObserver.onCompleted()
        }
    }

    fun CoroutineScope.produceMsgs() = produce<ByteArray> {
        val jedis = Jedis(REDIS_HOST)
        val ch = Channel<ByteArray>()

        GlobalScope.launch {
            jedis.subscribe(Subscriber(ch), BROADCAST)
            println("== stop to subscribe ==")
        }

        for (x in ch) {
            send(x)
        }
    }

    override fun receiveMsgs(request: ReceiveMsgsRequest, responseObserver: StreamObserver<ReceiveMsgsReply>) {
        val user = request.user
        println("user ${user.name} by ${Thread.currentThread().id}")

        runBlocking {
            val msgs = produceMsgs()
            msgs.consumeEach {
                println("get message ${Msg.parseFrom(it).content} by ${Thread.currentThread().id}")

                val reply = ReceiveMsgsReply
                    .newBuilder()
                    .setMsg(Msg.parseFrom(it))
                    .build()

                responseObserver.onNext(reply)
            }
        }
        // responseObserver.onCompleted()
    }

    inner class Subscriber(val ch: Channel<ByteArray>) : JedisPubSub() {
        override fun onMessage(channel: String, message: String) {
            runBlocking {
                println("${this}, send, through ${ch} by ${Thread.currentThread().id}")
                async {
                    ch.send(message.toByteArray())
                }
            }
        }
    }
}
