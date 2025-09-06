package com.wordOfTheDayApi.word_of_the_day_api.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.DefinitionDTO;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.RandomWordRequestDTO;
import com.wordOfTheDayApi.word_of_the_day_api.service.providers.DictionaryProvider;
import com.wordOfTheDayApi.word_of_the_day_api.service.providers.RandomWordApiProvider;
import com.wordOfTheDayApi.word_of_the_day_api.service.providers.WordProvider;

@Service
public class WordOfTheDayService {

    private final WordProvider wordProvider;      // Interface
    private final DictionaryProvider dictionaryProvider;
    private final Cache<String, Object> cache;

    public WordOfTheDayService(@Qualifier("randomWordApiProvider") WordProvider wordProvider,
                               DictionaryProvider dictionaryProvider) {
        this.wordProvider = wordProvider;        // Injects the qualified implementation
        this.dictionaryProvider = dictionaryProvider;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(10)
                .build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getWordOfTheDay() {
        String cacheKey = "wordOfTheDay";

        // Check cache first
        Object cached = cache.getIfPresent(cacheKey);
        if (cached != null) return (Map<String, Object>) cached;

        // Get random word from the qualified provider
        String word = wordProvider.getRandomWord();

        // Get definitions
        List<DefinitionDTO> definitions = dictionaryProvider.getDefinitions(word);

        // Build result map
        Map<String, Object> result = Map.of(
                "word", word,
                "definitions", definitions
        );

        // Cache it
        cache.put(cacheKey, result);

        return result;
    }

    public void clearCache() {
        cache.invalidate("wordOfTheDay");
    }
}
