package com.wordOfTheDayApi.word_of_the_day_api.service.providers;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.randomWord.RandomWordRequestDTO;

import org.springframework.beans.factory.annotation.Value;

/**
 * Provider that retrieves a random word from an external Random Word API
 * using Spring's reactive WebClient.
 */
@Service("randomWordApiProvider")
public class RandomWordApiProvider implements WordProvider {

    private final WebClient client;

    /**
     * Constructs a RandomWordApiProvider using a WebClient built with the provided base URL.
     *
     * @param builder            WebClient builder used to create the client instance.
     * @param randomWordApiUrl   Base URL of the external Random Word API.
     */
    public RandomWordApiProvider(WebClient.Builder builder,
                                 @Value("${external.api.base-url}") String randomWordApiUrl) {
        this.client = builder.baseUrl(randomWordApiUrl).build();
    }

    /**
     * Retrieves a random word from the external API according to the request options.
     * Currently the API returns an array, and we take the first element.
     *
     * @param request parameters for the random word retrieval (e.g., number of words)
     * @return the first random word returned by the API
     * @throws RuntimeException if the external API returns an error response
     */
    // Accept a request DTO
    public String getRandomWord(RandomWordRequestDTO request) {
        int number = request.number(); // currently always 1

        try {
            return client.get()
                    .uri(uriBuilder -> uriBuilder.path("/word").queryParam("number", number).build())
                    .retrieve()
                    .bodyToMono(String[].class)
                    .map(words -> words[0])
                    .block();
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException ex) {
            throw new RuntimeException("Random Word API failed: " + ex.getMessage());
        }
    }

    /**
     * Convenience method that delegates to {@link #getRandomWord(RandomWordRequestDTO)}
     * requesting a single word.
     */
    // Keep default method for backward compatibility
    @Override
    public String getRandomWord() {
        return getRandomWord(new RandomWordRequestDTO(1));
    }
}
