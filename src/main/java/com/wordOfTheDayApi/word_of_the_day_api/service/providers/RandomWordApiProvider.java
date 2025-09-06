package com.wordOfTheDayApi.word_of_the_day_api.service.providers;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import com.wordOfTheDayApi.word_of_the_day_api.exception.custom.WebClientResponseException;

@Service
public class RandomWordApiProvider implements WordProvider {

    private final WebClient client;

    public RandomWordApiProvider(WebClient.Builder builder,
                                 @Value("${external.api.base-url}") String randomWordApiUrl) {
        this.client = builder.baseUrl(randomWordApiUrl).build();
    }

    @Override
    public String getRandomWord() {
        try {
            return client.get()
                    .uri(uriBuilder -> uriBuilder.queryParam("words", 1).build())
                    .retrieve()
                    .bodyToMono(String[].class)
                    .map(words -> words[0])
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Random Word API failed: " + ex.getMessage());
        }
    }
}
