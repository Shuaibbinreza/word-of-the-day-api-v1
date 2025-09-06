package com.wordOfTheDayApi.word_of_the_day_api.model.dto.definitionDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DefinitionEntry(
        List<Meaning> meanings
) {}
