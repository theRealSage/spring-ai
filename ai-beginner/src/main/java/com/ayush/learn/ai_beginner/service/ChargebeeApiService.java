package com.ayush.learn.ai_beginner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChargebeeApiService {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final String site = "sagetest-test";
    private final String apiKey = "test_vdM9YOoE6Q1QPcd83RI3ybaeGhxF8xMkP";
    private Logger logger = LoggerFactory.getLogger(ChargebeeApiService.class);

    public ChargebeeApiService() {
    }

    @Tool(description = "Fetches a list of items from configured Chargebee account.")
    public String listItems(@ToolParam(description = "Number of items to fetch") Integer limit) throws Exception {
        logger.info("Tool invoked: listItems with limit: {}", limit);
        String baseUrl = "https://%s.chargebee.com/api/v2/item_prices".formatted(site);
        String finalUrl = baseUrl + "?limit=" + limit;
        String authString = apiKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(finalUrl))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("Response status code: {}", response.statusCode());
        logger.info("Response body: {}", response.body());
        return response.body();
    }

    @Tool(description = "Creates a Chargebee Quote given an item price id.")
    public String createQuote(@ToolParam(description = "Item price id of the item to be included in the quote") String itemPriceId) throws Exception {
        logger.info("Tool invoked: createQuote with itemPriceId: {}", itemPriceId);
        String url = "https://%s.chargebee.com/api/v2/customers/c1/create_subscription_quote_for_items".formatted(site);
        String authString = apiKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
        Map<String, String> formData = Map.ofEntries(
                Map.entry("subscription_items[item_price_id][0]", itemPriceId),
                Map.entry("subscription_items[quantity_in_decimal][0]", "1")
        );
        String formPayload = formData.entrySet()
                .stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + encodedAuth)
                .POST(HttpRequest.BodyPublishers.ofString(formPayload))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("Response status code: {}", response.statusCode());
        logger.info("Response body: {}", response.body());
        return response.body();
    }
}
