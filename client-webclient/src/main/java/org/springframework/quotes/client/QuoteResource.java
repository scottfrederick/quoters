package org.springframework.quotes.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuoteResource(String type, Quote value) { }
