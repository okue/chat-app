package com.okue.chat.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("greeting")
class Greeting {

    @GetMapping("/hello")
    fun hello(): String {
        return "hello"
    }
}
