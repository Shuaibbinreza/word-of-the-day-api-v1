package com.wordOfTheDayApi.word_of_the_day_api.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Meaning(
        String partOfSpeech,
        List<Def> definitions
) {
}
