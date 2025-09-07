package com.wordOfTheDayApi.word_of_the_day_api.service;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.WordOfTheDayResponseDTO;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.definitionDto.DefinitionDTO;
import com.wordOfTheDayApi.word_of_the_day_api.service.providers.DictionaryProvider;
import com.wordOfTheDayApi.word_of_the_day_api.service.providers.WordProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordOfTheDayServiceTest {

    @Mock
    private WordProvider wordProvider;

    @Mock
    private DictionaryProvider dictionaryProvider;

    private WordOfTheDayService service;

    @BeforeEach
    void setUp() {
        service = new WordOfTheDayService(wordProvider, dictionaryProvider);
    }

    @Test
    void getWordOfTheDay_shouldReturnWordAndDefinitions_andUseProvidersOnFirstCall() {
        // Arrange
        String word = "serendipity";
        List<DefinitionDTO> defs = List.of(
                new DefinitionDTO("the occurrence and development of events by chance in a happy or beneficial way", "noun"),
                new DefinitionDTO("an aptitude for making desirable discoveries by accident", "noun")
        );

        when(wordProvider.getRandomWord()).thenReturn(word);
        when(dictionaryProvider.getDefinitions(any())).thenReturn(defs);

        // Act
        WordOfTheDayResponseDTO response = service.getWordOfTheDay();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.word()).isEqualTo(word);
        assertThat(response.definitions()).containsExactlyElementsOf(defs);
        verify(wordProvider, times(1)).getRandomWord();
        verify(dictionaryProvider, times(1)).getDefinitions(any());
        verifyNoMoreInteractions(wordProvider, dictionaryProvider);
    }

    @Test
    void getWordOfTheDay_shouldUseCacheOnSubsequentCalls() {
        // Arrange
        String word = "ephemeral";
        List<DefinitionDTO> defs = List.of(new DefinitionDTO("lasting for a very short time", "adjective"));

        when(wordProvider.getRandomWord()).thenReturn(word);
        when(dictionaryProvider.getDefinitions(any())).thenReturn(defs);

        // Act
        WordOfTheDayResponseDTO first = service.getWordOfTheDay();
        WordOfTheDayResponseDTO second = service.getWordOfTheDay();

        // Assert
        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        // The service caches the built DTO instance, so it should be the exact same object
        assertThat(second).isSameAs(first);

        // Providers should be invoked only once thanks to caching
        verify(wordProvider, times(1)).getRandomWord();
        verify(dictionaryProvider, times(1)).getDefinitions(any());
        verifyNoMoreInteractions(wordProvider, dictionaryProvider);
    }

    @Test
    void clearCache_shouldInvalidateAndCauseProvidersToBeCalledAgain() {
        // Arrange
        when(wordProvider.getRandomWord()).thenReturn("alpha", "beta"); // first then second
        when(dictionaryProvider.getDefinitions(any()))
                .thenReturn(List.of(new DefinitionDTO("first def", "noun")))
                .thenReturn(List.of(new DefinitionDTO("second def", "noun")));

        // First call populates cache
        WordOfTheDayResponseDTO first = service.getWordOfTheDay();
        assertThat(first.word()).isEqualTo("alpha");

        // Clear cache
        service.clearCache();

        // Next call should fetch again (different values)
        WordOfTheDayResponseDTO second = service.getWordOfTheDay();
        assertThat(second.word()).isEqualTo("beta");

        // Providers should have been called twice total (once each time we fetched)
        verify(wordProvider, times(2)).getRandomWord();
        verify(dictionaryProvider, times(2)).getDefinitions(any());
        verifyNoMoreInteractions(wordProvider, dictionaryProvider);
    }

    @Test
    void getWordOfTheDay_shouldHandleEmptyDefinitionsList() {
        // Arrange
        String word = "minimal";
        when(wordProvider.getRandomWord()).thenReturn(word);
        when(dictionaryProvider.getDefinitions(any())).thenReturn(Collections.emptyList());

        // Act
        WordOfTheDayResponseDTO response = service.getWordOfTheDay();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.word()).isEqualTo(word);
        assertThat(response.definitions()).isEmpty();
    }
}
