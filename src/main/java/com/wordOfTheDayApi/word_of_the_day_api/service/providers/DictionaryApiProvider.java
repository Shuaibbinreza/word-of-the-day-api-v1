package com.wordOfTheDayApi.word_of_the_day_api.service.providers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.wordOfTheDayApi.word_of_the_day_api.exception.custom.WebClientResponseException;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.definitionDto.DefinitionDTO;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.definitionDto.DefinitionEntry;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.definitionDto.DictionaryRequestDTO;

import org.springframework.beans.factory.annotation.Value;

@Service("dictionaryApiProvider")
public class DictionaryApiProvider implements DictionaryProvider {

    private final WebClient client;

    public DictionaryApiProvider(WebClient.Builder builder,
                                 @Value("${external.dictionary-api.base-url}") String dictionaryApiUrl) {
        this.client = builder.baseUrl(dictionaryApiUrl).build();
    }

    @Override
    public List<DefinitionDTO> getDefinitions(DictionaryRequestDTO request) {
        String word = request.word();

        try {
            DefinitionEntry[] entries = client.get()
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
        }
    }
}

