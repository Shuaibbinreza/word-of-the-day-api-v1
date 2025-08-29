package com.wordOfTheDayApi.word_of_the_day_api.controller.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
    @GetMapping
    public String getWord() {
        return "Hello World";
    }
}
