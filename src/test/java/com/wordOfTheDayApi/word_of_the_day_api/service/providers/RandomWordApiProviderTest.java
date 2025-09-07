package com.wordOfTheDayApi.word_of_the_day_api.service.providers;

import com.wordOfTheDayApi.word_of_the_day_api.model.dto.randomWord.RandomWordRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Answers;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RandomWordApiProviderTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient webClient;

    private RandomWordApiProvider provider;

    @BeforeEach
    void setUp() {
        // Mock builder chain
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        // Deep stubs allow chaining like webClient.get().uri(...).retrieve().bodyToMono(...)

        provider = new RandomWordApiProvider(webClientBuilder, "https://example.com");
    }

    @Test
    void getRandomWord_withRequest_returnsFirstWordFromApi() {
        // Arrange
        when(webClient.get().uri(any(java.util.function.Function.class)).retrieve().bodyToMono(String[].class))
                        .thenReturn(Mono.just(new String[]{"alpha", "beta"}));

        // Act
        String result = provider.getRandomWord(new RandomWordRequestDTO(2));

        // Assert
        assertThat(result).isEqualTo("alpha");
    }

    @Test
    void getRandomWord_defaultMethod_delegatesToNumberOne() {
        // Arrange
        when(webClient.get().uri(any(java.util.function.Function.class)).retrieve().bodyToMono(String[].class))
                        .thenReturn(Mono.just(new String[]{"solo"}));

        // Act
        String result = provider.getRandomWord();

        // Assert
        assertThat(result).isEqualTo("solo");
    }

    @Test
    void getRandomWord_whenWebClientResponseExceptionThrown_wrapsIntoRuntimeException() {
        // Arrange
        org.springframework.web.reactive.function.client.WebClientResponseException underlying =
                new org.springframework.web.reactive.function.client.WebClientResponseException(
                        "Bad Gateway", 502, "Bad Gateway", null, null, null);
        when(webClient.get().uri(any(java.util.function.Function.class)).retrieve().bodyToMono(String[].class))
                        .thenReturn(Mono.error(underlying));

        // Act & Assert
        assertThatThrownBy(() -> provider.getRandomWord(new RandomWordRequestDTO(1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Random Word API failed:")
                .hasMessageContaining("Bad Gateway");
    }
}
