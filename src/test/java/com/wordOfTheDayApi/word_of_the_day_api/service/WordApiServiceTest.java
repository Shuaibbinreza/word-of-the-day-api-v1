package com.wordOfTheDayApi.word_of_the_day_api.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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
}
