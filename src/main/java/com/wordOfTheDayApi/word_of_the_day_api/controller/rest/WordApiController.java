package com.wordOfTheDayApi.word_of_the_day_api.controller.rest;

import com.wordOfTheDayApi.word_of_the_day_api.service.WordApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for providing Word of the Day endpoints.
 * <p>
 * This controller exposes endpoints to fetch the word of the day along with its definitions,
 * and also allows refreshing the cached word.
 * </p>
 */
@RestController
@Tag(name = "Word of the Day Rest Resource", description = "Endpoints for Word of the Day")
public class WordApiController {

    private final WordApiService apiService;

    /**
     * Constructs a new WordApiController with the specified WordApiService.
     *
     * @param apiService the WordApiService used to fetch words and definitions
     */
    public WordApiController(WordApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Retrieves the current Word of the Day along with its definitions.
     * <p>
     * If the word is already cached, it will return the cached value.
     * Otherwise, it will fetch a new random word and cache it.
     * </p>
     *
     * @return an Object containing the word under "word" key and its definitions under "definitions" key
     */
    @Operation(summary = "Get Word of the Day", description = "Fetches the current Word of the Day along with definitions")
    @GetMapping("/wordOfTheDay")
    public Object getWordOfTheDay() {
        return apiService.getRandomWordWithDefinition();
    }

    /**
     * Clears the cached Word of the Day and fetches a fresh random word with definitions.
     *
     * @return an Object containing the newly fetched word and definitions
     */
    @Operation(summary = "Refresh Word of the Day", description = "Clears cache and fetches a new Word of the Day")
    @GetMapping("/wordOfTheDay/refresh")
    public Object refreshWordOfTheDay() {
        apiService.clearWordOfTheDayCache();
        return apiService.getRandomWordWithDefinition();
    }
}
