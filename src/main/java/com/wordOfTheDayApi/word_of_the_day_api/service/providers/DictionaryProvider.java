package com.wordOfTheDayApi.word_of_the_day_api.service.providers;

import java.util.List;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.DefinitionDTO;

public interface DictionaryProvider {
    List<DefinitionDTO> getDefinitions(String word);
}
