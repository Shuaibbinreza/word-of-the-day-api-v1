package com.wordOfTheDayApi.word_of_the_day_api.service.providers;

import java.util.List;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.definitionDto.DefinitionDTO;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.definitionDto.DictionaryRequestDTO;

public interface DictionaryProvider {
    List<DefinitionDTO> getDefinitions(DictionaryRequestDTO request);
}
