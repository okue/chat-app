package com.okue.chat

import com.okue.controller.Msg
import com.okue.controller.ReceiveMsgsReply
import redis.clients.jedis.JedisPubSub
import java.util.logging.Logger

object Subscriber : JedisPubSub() {
    private var logger = Logger.getLogger(this::class.java.name)

    override fun onMessage(channel: String, message: String) {
        logger.info("${Thread.currentThread().name} gets ${message} on ${channel}")
    }

    override fun onPMessage(pattern: String, channel: String, message: String) {
        logger.info("${Thread.currentThread().name} gets ${message} on ${channel}")
        try {
            val userName = channel.substring(7)
            logger.info("${userName}")
            val observer = Observers.get(userName)
            val reply =
                ReceiveMsgsReply
                    .newBuilder()
                    .setMsg(Msg.parseFrom(message.toByteArray()))
            observer!!.onNext(reply.build())
        } catch (e: Exception) {
            logger.warning("something bad happens")
        }
    }
}

