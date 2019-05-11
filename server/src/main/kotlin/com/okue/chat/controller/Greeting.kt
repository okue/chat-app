package com.okue.chat.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Logger

@RestController
@RequestMapping("greeting")
class Greeting {

    private val logger = Logger.getLogger(this::class.java.name)

    @GetMapping("/hello")
    fun hello(): String {
        return "hello"
    }
}
