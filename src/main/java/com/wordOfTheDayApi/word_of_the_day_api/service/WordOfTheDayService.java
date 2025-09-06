package com.wordOfTheDayApi.word_of_the_day_api.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.WordOfTheDayResponseDTO;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.definitionDto.DefinitionDTO;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.definitionDto.DictionaryRequestDTO;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.randomWord.RandomWordRequestDTO;
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
    public WordOfTheDayResponseDTO getWordOfTheDay() {
        String cacheKey = "wordOfTheDay";

        // Check cache first
        Object cached = cache.getIfPresent(cacheKey);
        if (cached != null) return (WordOfTheDayResponseDTO) cached;

        // Get random word from the qualified provider
        String word = wordProvider.getRandomWord();

        // Get definitions
        DictionaryRequestDTO dictRequest = new DictionaryRequestDTO(word);
        List<DefinitionDTO> definitions = dictionaryProvider.getDefinitions(dictRequest);

        // Build response DTO
        WordOfTheDayResponseDTO result = new WordOfTheDayResponseDTO(word, definitions);

        // Cache it
        cache.put(cacheKey, result);

        return result;
    }

    public void clearCache() {
        cache.invalidate("wordOfTheDay");
    }
}
