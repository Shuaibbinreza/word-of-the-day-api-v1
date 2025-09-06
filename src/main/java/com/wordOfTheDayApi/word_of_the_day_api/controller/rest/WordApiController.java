package com.wordOfTheDayApi.word_of_the_day_api.controller.rest;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.WordOfTheDayResponseDTO;
import com.wordOfTheDayApi.word_of_the_day_api.service.WordOfTheDayService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Word of the day Rest Resource", description = "Endpoints for word of the day")
public class WordApiController {
    // private final WordApiService apiService;
    private final WordOfTheDayService apiService;

    public WordApiController(WordOfTheDayService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/wordOfTheDay")
    public WordOfTheDayResponseDTO getWordOfTheDay() {
        return apiService.getWordOfTheDay();
    }

    @GetMapping("/wordOfTheDay/refresh")
    public WordOfTheDayResponseDTO refreshWordOfTheDay() {
        apiService.clearCache();
        return apiService.getWordOfTheDay();
    }
}
