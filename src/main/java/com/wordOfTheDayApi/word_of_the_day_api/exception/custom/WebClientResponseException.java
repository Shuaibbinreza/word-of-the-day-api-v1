package com.wordOfTheDayApi.word_of_the_day_api.exception.custom;

import io.micrometer.common.lang.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientException;

public class WebClientResponseException extends WebClientException {

    private final int rawStatusCode;
    private final HttpStatus statusCode;
    private final String statusText;
    private final HttpHeaders headers;
    @Nullable
    private final byte[] responseBody;

    // Constructor
    protected WebClientResponseException(String msg, int rawStatusCode, HttpStatus statusCode,
                                         String statusText, HttpHeaders headers,
                                         @Nullable byte[] responseBody) {
        super(msg);
        this.rawStatusCode = rawStatusCode;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = headers;
        this.responseBody = responseBody;
    }

    // Getters
    public int getRawStatusCode() {
        return this.rawStatusCode;
    }

    public HttpStatus getStatusCode() {
        return this.statusCode;
    }

    public String getStatusText() {
        return this.statusText;
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    @Nullable
    public byte[] getResponseBodyAsByteArray() {
        return this.responseBody;
    }

    @Nullable
    public String getResponseBodyAsString() {
        return (this.responseBody != null ? new String(this.responseBody) : null);
    }
}
