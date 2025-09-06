package com.wordOfTheDayApi.word_of_the_day_api.model.dto;

import java.util.List;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.definitionDto.DefinitionDTO;

public record WordOfTheDayResponseDTO(
        String word,
        List<DefinitionDTO> definitions
) {}
