package com.wordOfTheDayApi.word_of_the_day_api.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.DefinitionDTO;
import com.wordOfTheDayApi.word_of_the_day_api.model.dto.DefinitionEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service responsible for fetching a random word and its definitions
 * using external APIs and caching the result for 24 hours.
 */
@Service
public class WordApiService {

    private static final Logger logger = LoggerFactory.getLogger(WordApiService.class);
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(1);

    private final WebClient randomWordClient;
    private final WebClient dictionaryClient;
    private final Cache<String, Object> cache;

    /**
     * Constructs a new WordApiService with the specified WebClient builders
     * and API URLs.
     *
     * @param webClientBuilder the WebClient.Builder used to build WebClient instances
     * @param randomWordApiUrl the base URL for the random word API
     * @param dictionaryApiUrl the base URL for the dictionary API
     */
    public WordApiService(WebClient.Builder webClientBuilder,
                          @Value("${external.api.base-url}") String randomWordApiUrl,
                          @Value("${external.dictionary-api.base-url}") String dictionaryApiUrl) {
        this.randomWordClient = webClientBuilder.baseUrl(randomWordApiUrl).build();
        this.dictionaryClient = webClientBuilder.baseUrl(dictionaryApiUrl).build();

        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(10)
                .build();
    }

    /**
     * Fetches a random word from the external random word API.
     * Implements retry logic to handle temporary failures.
     *
     * @return a random word as a String
     * @throws RuntimeException if the API call fails after retries or any error occurs
     */
    private String fetchRandomWord() {
        try {
            return randomWordClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/word").queryParam("number", 1).build())
                    .retrieve()
                    .bodyToMono(String[].class)
                    .map(words -> {
                        if (words == null || words.length == 0) {
                            logger.warn("Random Word API returned empty response");
                            throw new RuntimeException("Random Word API returned empty response");
                        }
                        return words[0];
                    })
                    .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                            .filter(ex -> shouldRetry(ex))
                            .doBeforeRetry(retrySignal -> 
                                logger.warn("Retrying random word API call after failure. Attempt: {}, Error: {}", 
                                    retrySignal.totalRetries() + 1, 
                                    retrySignal.failure().getMessage())))
                    .block();
        } catch (WebClientResponseException ex) {
            logger.error("Random Word API call failed after {} retries: {}", MAX_RETRIES, ex.getMessage());
            throw new RuntimeException("Random Word API call failed: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error fetching random word after {} retries: {}", MAX_RETRIES, ex.getMessage());
            throw new RuntimeException("Error fetching random word: " + ex.getMessage());
        }
    }

    /**
     * Determines if a retry should be attempted based on the exception type.
     * 
     * @param throwable the exception that occurred
     * @return true if the operation should be retried, false otherwise
     */
    private boolean shouldRetry(Throwable throwable) {
        // Retry on connection issues, timeouts, or 5xx server errors
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException wcre = (WebClientResponseException) throwable;
            int statusCode = wcre.getStatusCode().value();
            return statusCode >= 500 && statusCode < 600;
        }

        // Also retry on network-related exceptions
        return throwable instanceof java.net.ConnectException ||
               throwable instanceof java.net.SocketTimeoutException ||
               throwable instanceof java.io.IOException;
    }

    /**
     * Fetches the definitions of a given word from the dictionary API.
     * Implements retry logic to handle temporary failures.
     *
     * @param word the word to fetch definitions for
     * @return a list of DefinitionDTO objects representing the word's definitions
     * @throws RuntimeException if the API call fails after retries or any error occurs
     */
    private List<DefinitionDTO> fetchWordDefinition(String word) {
        try {
            DefinitionEntry[] entries = dictionaryClient.get()
                    .uri("/en/{word}", word)
                    .retrieve()
                    .bodyToMono(DefinitionEntry[].class)
                    .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                            .filter(ex -> shouldRetry(ex))
                            .doBeforeRetry(retrySignal -> 
                                logger.warn("Retrying dictionary API call for word '{}' after failure. Attempt: {}, Error: {}", 
                                    word,
                                    retrySignal.totalRetries() + 1, 
                                    retrySignal.failure().getMessage())))
                    .block();

            if (entries == null || entries.length == 0) {
                logger.info("No definitions found for word: '{}'", word);
                return Collections.emptyList();
            }

            List<DefinitionDTO> definitions = Arrays.stream(entries)
                    .flatMap(entry -> entry.meanings().stream())
                    .flatMap(meaning -> meaning.definitions().stream()
                            .map(def -> new DefinitionDTO(def.definition(), meaning.partOfSpeech())))
                    .collect(Collectors.toList());

            logger.debug("Found {} definitions for word: '{}'", definitions.size(), word);
            return definitions;

        } catch (WebClientResponseException ex) {
            logger.error("Dictionary API call failed for word '{}' after {} retries: {}", word, MAX_RETRIES, ex.getMessage());
            throw new RuntimeException("Dictionary API call failed for word '" + word + "': " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error fetching definition for word '{}' after {} retries: {}", word, MAX_RETRIES, ex.getMessage());
            throw new RuntimeException("Error fetching definition for word '" + word + "': " + ex.getMessage());
        }
    }

    /**
     * Retrieves a random word along with its definitions.
     * The result is cached for 24 hours to prevent repeated API calls.
     * Implements fallback mechanisms to handle API failures.
     *
     * @return a map containing the word under "word" key and a list of definitions under "definitions" key
     */
    public Map<String, Object> getRandomWordWithDefinition() {
        String cacheKey = "wordOfTheDay";

        synchronized (this) {
            // Try to return cached result first
            Object cached = cache.getIfPresent(cacheKey);
            if (cached != null) {
                logger.debug("Returning cached word of the day");
                return (Map<String, Object>) cached;
            }

            logger.info("Cache miss for word of the day, fetching new data");

            try {
                // Try to fetch a random word
                String word = fetchRandomWord();
                List<DefinitionDTO> definitions;

                try {
                    // Try to fetch definitions for the word
                    definitions = fetchWordDefinition(word);
                } catch (Exception ex) {
                    // If fetching definitions fails, log and return empty definitions
                    logger.error("Failed to fetch definitions for word '{}', returning word with empty definitions", word);
                    definitions = Collections.emptyList();
                }

                Map<String, Object> result = Map.of(
                        "word", word,
                        "definitions", definitions
                );

                // Cache the result
                cache.put(cacheKey, result);
                logger.info("Successfully fetched and cached new word of the day: '{}'", word);
                return result;

            } catch (Exception ex) {
                // If fetching random word fails, try to provide a fallback
                logger.error("Failed to fetch random word: {}", ex.getMessage());

                // Fallback to a default word if everything fails
                String fallbackWord = "fallback";
                List<DefinitionDTO> fallbackDefinitions = Collections.singletonList(
                    new DefinitionDTO("A contingency option to be taken if the primary option fails", "noun")
                );

                Map<String, Object> fallbackResult = Map.of(
                        "word", fallbackWord,
                        "definitions", fallbackDefinitions,
                        "error", "Failed to fetch data from external APIs"
                );

                // Don't cache the fallback result
                logger.warn("Returning fallback word of the day due to API failures");
                return fallbackResult;
            }
        }
    }

    /**
     * Clears the cached "Word of the Day".
     * Useful if you want to force fetching a new word before the cache expires.
     */
    public void clearWordOfTheDayCache() {
        cache.invalidate("wordOfTheDay");
    }
}