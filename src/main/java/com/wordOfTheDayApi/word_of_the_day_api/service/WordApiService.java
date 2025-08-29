package com.wordOfTheDayApi.word_of_the_day_api.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.DefinitionDTO;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.DefinitionEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class WordApiService {

    private final WebClient randomWordClient;
    private final WebClient dictionaryClient;
    private final Cache<String, Object> cache;

    public WordApiService(WebClient.Builder webClientBuilder,
                          @Value("${external.api.base-url}") String randomWordApiUrl,
                          @Value("${external.dictionary-api.base-url}") String dictionaryApiUrl) {
        this.randomWordClient = webClientBuilder.baseUrl(randomWordApiUrl).build();
        this.dictionaryClient = webClientBuilder.baseUrl(dictionaryApiUrl).build();

        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(10)
                .build();
    }

    private String fetchRandomWord() {
        try {
            return randomWordClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/word").queryParam("number", 1).build())
                    .retrieve()
                    .bodyToMono(String[].class)
                    .map(words -> words[0])
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Random Word API call failed: " + ex.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching random word: " + ex.getMessage());
        }
    }

    private List<DefinitionDTO> fetchWordDefinition(String word) {
        try {
            DefinitionEntry[] entries = dictionaryClient.get()
                    .uri("/en/{word}", word)
                    .retrieve()
                    .bodyToMono(DefinitionEntry[].class)
                    .block();

            if (entries == null || entries.length == 0) {
                return Collections.emptyList();
            }

            return Arrays.stream(entries)
                    .flatMap(entry -> entry.meanings().stream())
                    .flatMap(meaning -> meaning.definitions().stream()
                            .map(def -> new DefinitionDTO(def.definition(), meaning.partOfSpeech())))
                    .collect(Collectors.toList());

        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Dictionary API call failed for word '" + word + "': " + ex.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching definition for word '" + word + "': " + ex.getMessage());
        }
    }

    public Map<String, Object> getRandomWordWithDefinition() {
        String cacheKey = "wordOfTheDay";

        synchronized (this) {
            Object cached = cache.getIfPresent(cacheKey);
            if (cached != null) return (Map<String, Object>) cached;

            String word = fetchRandomWord();
            List<DefinitionDTO> definitions = fetchWordDefinition(word);

            Map<String, Object> result = Map.of(
                    "word", word,                 // Added the word key
                    "definitions", definitions    // Definitions list
            );

            cache.put(cacheKey, result);
            return result;
        }
    }

    public void clearWordOfTheDayCache() {
        cache.invalidate("wordOfTheDay");
    }
}
