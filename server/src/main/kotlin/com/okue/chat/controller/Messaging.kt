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
import org.lognet.springboot.grpc.GRpcService

// NOTE
// https://github.com/LogNet/grpc-spring-boot-starter

// NOTE for grpcc
// client.receiveMsgs({user : { name : "fuga" }}).on('data', sr).on('status', sr)

@GRpcService
class Messaging : MessagingGrpc.MessagingImplBase() {
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
        repeat(3) {
            responseObserver.onNext(reply)
        }
        responseObserver.onCompleted()
    }
}