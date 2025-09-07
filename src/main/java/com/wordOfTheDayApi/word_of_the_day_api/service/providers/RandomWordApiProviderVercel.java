package com.wordOfTheDayApi.word_of_the_day_api.service.providers;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.randomWord.RandomWordRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service("randomWordApiProviderVercel")
public class RandomWordApiProviderVercel implements WordProvider {

    private final WebClient client;

    public RandomWordApiProviderVercel(WebClient.Builder builder,
                                       @Value("${external.api.base-url-vercel}") String randomWordApiUrl) {
        this.client = builder.baseUrl(randomWordApiUrl).build();
    }

    // Accept a request DTO
    public String getRandomWord(RandomWordRequestDTO request) {
        int number = request.number(); // currently always 1

        try {
            return client.get()
                    .uri(uriBuilder -> uriBuilder.queryParam("number", number).build())
                    .retrieve()
                    .bodyToMono(String[].class)
                    .map(words -> words[0])
                    .block();
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException ex) {
            throw new RuntimeException("Random Word API failed: " + ex.getMessage());
        }
    }

    // Keep default method for backward compatibility
    @Override
    public String getRandomWord() {
        return getRandomWord(new RandomWordRequestDTO(1));
    }
}
