package com.okue.chat.controller

import com.okue.controller.MessagingGrpc
import com.okue.controller.Reply
import com.okue.controller.SendMsgReply
import com.okue.controller.SendMsgRequest
import io.grpc.stub.StreamObserver
import org.lognet.springboot.grpc.GRpcService

// NOTE
// https://github.com/LogNet/grpc-spring-boot-starter

// NOTE for grpcc
// client.sendMsg({user: {name: "hoge"}, msg: {from: { name: "hoge" }, to: { name : "fuga" }, content: "aaaaa"}}, pr)

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
}