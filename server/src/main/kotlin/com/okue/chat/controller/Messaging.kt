package com.okue.chat.controller

import com.okue.controller.MessagingGrpc
import com.okue.controller.Msg
import com.okue.controller.ReceiveMsgsReply
import com.okue.controller.ReceiveMsgsRequest
import com.okue.controller.Reply
import com.okue.controller.SendMsgReply
import com.okue.controller.SendMsgRequest
import com.okue.controller.User
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

@GRpcService
class Messaging : MessagingGrpc.MessagingImplBase() {
    companion object

    val jedis = Jedis()

    override fun sendMsg(request: SendMsgRequest, responseObserver: StreamObserver<SendMsgReply>) {
        val msg = request.msg
        println("from ${msg.from}, to ${msg.to}, content ${msg.content}")
        val reply = Reply
            .newBuilder()
            .setResult(Reply.Result.Ok)
            .setReason("")
            .build()
        val reply2 = SendMsgReply
            .newBuilder()
            .setReply(reply)
            .build()
        responseObserver.onNext(reply2)
        responseObserver.onCompleted()
    }

    override fun receiveMsgs(request: ReceiveMsgsRequest, responseObserver: StreamObserver<ReceiveMsgsReply>) {
        val user = request.user
        println("user ${user.name}")
        val msg = Msg
            .newBuilder()
            .setFrom(User.newBuilder().setName("sakana"))
            .setTo(User.newBuilder().setName("misoyaki"))
        val reply = ReceiveMsgsReply
            .newBuilder()
            .setMsg(msg)
            .build()

        val ch = Channel<String>()

        GlobalScope.async {
            jedis.subscribe(Subscriber(ch), "hoge")
        }

        GlobalScope.launch {
            for (m in ch) {
                responseObserver.onNext(reply)
            }
        }
        // responseObserver.onCompleted()
    }

    inner class Subscriber(val ch: Channel<String>) : JedisPubSub() {
        override fun onMessage(channel: String, message: String) {
            println(this)
            println("get message '${message}' at the channel ${channel}")
            GlobalScope.async {
                ch.send(message)
            }
        }
    }
}
