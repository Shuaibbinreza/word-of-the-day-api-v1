package com.wordOfTheDayApi.word_of_the_day_api.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.DefinitionDTO;
import com.wordOfTheDayApi.word_of_the_day_api.service.providers.DictionaryProvider;
import com.wordOfTheDayApi.word_of_the_day_api.service.providers.WordProvider;

@Service
public class WordOfTheDayService {

    private final WordProvider wordProvider;
    private final DictionaryProvider dictionaryProvider;
    private final Cache<String, Object> cache;

    public WordOfTheDayService(WordProvider wordProvider, DictionaryProvider dictionaryProvider) {
        this.wordProvider = wordProvider;
        this.dictionaryProvider = dictionaryProvider;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(10)
                .build();
    }

    public Map<String, Object> getWordOfTheDay() {
        String cacheKey = "wordOfTheDay";

        Object cached = cache.getIfPresent(cacheKey);
        if (cached != null) return (Map<String, Object>) cached;

        String word = wordProvider.getRandomWord();
        List<DefinitionDTO> definitions = dictionaryProvider.getDefinitions(word);

        Map<String, Object> result = Map.of(
                "word", word,
                "definitions", definitions
        );

        cache.put(cacheKey, result);
        return result;
    }

    public void clearCache() {
        cache.invalidate("wordOfTheDay");
    }
}
