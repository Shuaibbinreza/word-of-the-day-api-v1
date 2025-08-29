package com.wordOfTheDayApi.word_of_the_day_api.controller.rest;

import com.wordOfTheDayApi.word_of_the_day_api.service.WordApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Word of the day Rest Resource", description = "Endpoints for word of the day")
public class WordApiController {
    private final WordApiService apiService;

    public WordApiController(WordApiService apiService) {
        this.apiService = apiService;
    }

    @Operation(summary = "Get word of the day")
    @GetMapping("/wordOfTheDay")
    public Object getWordOfTheDay() {
        return apiService.getRandomWordWithDefinition();
    }

    @Operation(summary = "Get word of the day with cleared cache")
    @GetMapping("/wordOfTheDay/refresh")
    public Object refreshWordOfTheDay() {
        apiService.clearWordOfTheDayCache();
        return apiService.getRandomWordWithDefinition();
    }
}
