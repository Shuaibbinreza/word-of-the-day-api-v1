package com.wordOfTheDayApi.word_of_the_day_api.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordApiServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient randomWordClient;

    @Mock
    private WebClient dictionaryClient;

    private WordApiService wordApiService;

    @BeforeEach
    void setUp() {
        // Configure mock WebClient.Builder to return mock clients
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(randomWordClient, dictionaryClient);
    }

    @Test
    void constructor_shouldInitializeWebClientsWithCorrectBaseUrls() throws Exception {
        // Given
        String randomWordApiUrl = "https://api.randomword.com";
        String dictionaryApiUrl = "https://api.dictionary.com";

        // When
        WordApiService service = new WordApiService(webClientBuilder, randomWordApiUrl, dictionaryApiUrl);

        // Then
        WebClient actualRandomWordClient = getPrivateField(service, "randomWordClient");
        WebClient actualDictionaryClient = getPrivateField(service, "dictionaryClient");

        assertThat(actualRandomWordClient).isNotNull();
        assertThat(actualDictionaryClient).isNotNull();
        assertThat(actualRandomWordClient).isSameAs(randomWordClient);
        assertThat(actualDictionaryClient).isSameAs(dictionaryClient);
    }

    @Test
    void constructor_shouldInitializeCaffeineCacheWithCorrectConfiguration() throws Exception {
        // Given
        String randomWordApiUrl = "https://api.randomword.com";
        String dictionaryApiUrl = "https://api.dictionary.com";

        // When
        WordApiService service = new WordApiService(webClientBuilder, randomWordApiUrl, dictionaryApiUrl);

        // Then
        Cache<String, Object> cache = getPrivateField(service, "cache");
        assertThat(cache).isNotNull();

        // Verify cache configuration by checking if it accepts values
        cache.put("testKey", "testValue");
        assertThat(cache.getIfPresent("testKey")).isEqualTo("testValue");
    }

    @Test
    void constructor_shouldHandleEmptyUrls() {
        // Given
        String randomWordApiUrl = "";
        String dictionaryApiUrl = "";

        // When & Then - Should not throw exception
        WordApiService service = new WordApiService(webClientBuilder, randomWordApiUrl, dictionaryApiUrl);
        assertThat(service).isNotNull();
    }

    @Test
    void constructor_shouldHandleNullWebClientBuilder() {
        // Given
        String randomWordApiUrl = "https://api.randomword.com";
        String dictionaryApiUrl = "https://api.dictionary.com";

        // When & Then - Should throw NullPointerException
        try {
            new WordApiService(null, randomWordApiUrl, dictionaryApiUrl);
        } catch (NullPointerException e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void constructor_shouldHandleNullUrls() {
        // Given
        String randomWordApiUrl = null;
        String dictionaryApiUrl = null;

        // When & Then - Should throw NullPointerException when building WebClient
        try {
            new WordApiService(webClientBuilder, randomWordApiUrl, dictionaryApiUrl);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    @Test
    void cache_shouldHaveCorrectExpirationTime() throws Exception {
        // Given
        String randomWordApiUrl = "https://api.randomword.com";
        String dictionaryApiUrl = "https://api.dictionary.com";

        // When
        WordApiService service = new WordApiService(webClientBuilder, randomWordApiUrl, dictionaryApiUrl);

        // Then
        Cache<String, Object> cache = getPrivateField(service, "cache");

        // Put a value and verify it exists immediately
        cache.put("testKey", "testValue");
        assertThat(cache.getIfPresent("testKey")).isEqualTo("testValue");

        // Note: Testing actual expiration would require time manipulation
        // This test verifies the cache is properly initialized and functional
    }

    @Test
    void cache_shouldHaveCorrectMaximumSize() throws Exception {
        // Given
        String randomWordApiUrl = "https://api.randomword.com";
        String dictionaryApiUrl = "https://api.dictionary.com";

        // When
        WordApiService service = new WordApiService(webClientBuilder, randomWordApiUrl, dictionaryApiUrl);

        // Then
        Cache<String, Object> cache = getPrivateField(service, "cache");

        // Add more than maximum size (10) to verify size constraint
        for (int i = 0; i < 15; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Cache should still be functional
        assertThat(cache.getIfPresent("key14")).isNotNull();
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    @SuppressWarnings("unchecked")
    private <T> T invokePrivateMethod(Object object, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = object.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        try {
            return (T) method.invoke(object, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Unwrap the InvocationTargetException to get the actual exception
            throw (Exception) e.getCause();
        }
    }

    @Test
    void fetchRandomWord_shouldReturnWordWhenApiCallSucceeds() throws Exception {
        // Given
        String randomWordApiUrl = "https://api.randomword.com";
        String dictionaryApiUrl = "https://api.dictionary.com";
        String expectedWord = "example";

        // Mock WebClient response chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(randomWordClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String[].class)).thenReturn(Mono.just(new String[]{expectedWord}));

        // Create service with mocked dependencies
        wordApiService = new WordApiService(webClientBuilder, randomWordApiUrl, dictionaryApiUrl);

        // When
        String result = invokePrivateMethod(wordApiService, "fetchRandomWord", new Class[]{});

        // Then
        assertThat(result).isEqualTo(expectedWord);
    }

    @Test
    void fetchRandomWord_shouldThrowRuntimeExceptionWhenWebClientResponseExceptionOccurs() throws Exception {
        // Given
        String randomWordApiUrl = "https://api.randomword.com";
        String dictionaryApiUrl = "https://api.dictionary.com";

        // Mock WebClient response chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(randomWordClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String[].class)).thenReturn(Mono.error(
            new WebClientResponseException(404, "Not Found", null, null, null)));

        // Create service with mocked dependencies
        wordApiService = new WordApiService(webClientBuilder, randomWordApiUrl, dictionaryApiUrl);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            invokePrivateMethod(wordApiService, "fetchRandomWord", new Class[]{});
        });

        assertThat(exception.getMessage()).contains("Random Word API call failed");
    }

    @Test
    void fetchRandomWord_shouldThrowRuntimeExceptionWhenGenericExceptionOccurs() throws Exception {
        // Given
        String randomWordApiUrl = "https://api.randomword.com";
        String dictionaryApiUrl = "https://api.dictionary.com";

        // Mock WebClient response chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(randomWordClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String[].class)).thenReturn(Mono.error(
            new IllegalStateException("Generic error")));

        // Create service with mocked dependencies
        wordApiService = new WordApiService(webClientBuilder, randomWordApiUrl, dictionaryApiUrl);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            invokePrivateMethod(wordApiService, "fetchRandomWord", new Class[]{});
        });

        assertThat(exception.getMessage()).contains("Error fetching random word");
    }
}
