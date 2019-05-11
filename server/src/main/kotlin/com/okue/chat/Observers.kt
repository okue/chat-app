package com.okue.chat

import com.okue.controller.ReceiveMsgsReply
import io.grpc.stub.StreamObserver
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

object Observers {
    private val map = ConcurrentHashMap<String, StreamObserver<ReceiveMsgsReply>>()

    private val logger = Logger.getLogger(this::class.java.name)

    fun get(name: String): StreamObserver<ReceiveMsgsReply>? {
        return this.map.get(name)
    }

    fun put(name: String, observer: StreamObserver<ReceiveMsgsReply>) {
        this.map.put(name, observer)
    }

    fun del(name: String) {
        this.map.remove(name)
    }
}