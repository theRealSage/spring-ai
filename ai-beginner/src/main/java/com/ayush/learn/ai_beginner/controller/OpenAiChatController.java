package com.ayush.learn.ai_beginner.controller;

import com.ayush.learn.ai_beginner.model.PriceAnalysis;
import com.ayush.learn.ai_beginner.model.QuoteRequest;
import com.ayush.learn.ai_beginner.service.ChargebeeApiService;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class OpenAiChatController {

    private final ChatClient chatClient;
    private final ChargebeeApiService chargebeeApiService;
    private final Logger logger = LoggerFactory.getLogger(OpenAiChatController.class);

    public OpenAiChatController(@Qualifier("openAIChatClient") ChatClient chatClient, ChargebeeApiService chargebeeApiService) {
        this.chatClient = chatClient;
        this.chargebeeApiService = chargebeeApiService;
    }

    @GetMapping("/")
    public String demo() {
        return "demo";
    }

    @GetMapping("/1")
    public String simpleChatClientDemo() {
        String question = "What is Chargebee? How can it help me?";
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    @GetMapping("/2")
    public String simplePromptTemplateDemo(@RequestParam("org") String organisation) {
        String template = "What are the pricing plans offered by {organisation}?";
//        PromptTemplate promptTemplate = PromptTemplate.builder()
//                .template(template)
//                .build();
//        Prompt prompt = promptTemplate.create();
//        String answer = chatClient.prompt(prompt).call().content();
        return chatClient.prompt()
                .user(u -> {
                    u.text(template);
                    u.param("organisation", organisation);
                })
                .call()
                .content();
    }

    @GetMapping("/3")
    public String systemPromptsDemo(@RequestParam("org") String organisation) {
        String template = "What are the pricing plans offered by {organisation}?";
        String systemPrompt = """
                Always let the user know your last training date.
                """;
        return chatClient.prompt()
                .system(systemPrompt)
                .user(u -> {
                    u.text(template);
                    u.param("organisation", organisation);
                })
                .call()
                .content();
    }

    @GetMapping("/4")
    public PriceAnalysis structuredOutputsDemo(@RequestParam("org") String organisation) {
        String template = "What are the pricing plans offered by {organisation}?";
        String systemPrompt = """
                Overview:
                You are an AI assistant that helps user gather the pricing information of digital products and services.
                
                Rules:
                1. Always provide structured output in the form of a JSON object.
                2. If the organisation provided in the input does not sell digital products or services, return an empty list in the `plans` field.
                3. If the structured output requires a disclaimer, include it in the `disclaimer` field. 
                `disclaimer` should include 
                    - Standard disclaimer like `This is an ai generated response and may not be accurate. Please verify with the official website of the organisation.`.
                    - Your last training date
                    - Call out if the organisation provided in the input does not deal in digital products or services.
                """;
        PriceAnalysis structuredAnswer = chatClient.prompt()
                .system(systemPrompt)
                .user(u -> {
                    u.text(template);
                    u.param("organisation", organisation);
                })
                .call()
                .entity(PriceAnalysis.class);
        return structuredAnswer;
    }

    @GetMapping("/5")
    public String toolCallsDemo(@RequestParam(value = "curr") String currency, @RequestParam(value = "model") String pricingModel) {
        logger.info("Executing first prompt with currency: {}, pricing model: {}", currency, pricingModel);
        String template1 = "Check the plans in my chargebee account suggest one with currency as {curr} and pricing model as {pricing-model}. ";
        String systemPrompt = """
                - You are a personal ai assistant that helps user interact with Chargebee API.
                - Chargebee account is already configured with the required API keys.
                - You are given access to Chargebee API service that can be used to interact with Chargebee.
                - If you are not sure of the user's intent, ask for clarification.
                - If you cannot fulfill the user's request, politely inform them that you are unable to do so.
                """;
        QuoteRequest quoteRequest = chatClient.prompt()
                .system(systemPrompt)
                .user(u -> {
                    u.text(template1);
                    u.param("curr", currency);
                    u.param("pricing-model", pricingModel);
                })
                .tools(chargebeeApiService)
                .call()
                .entity(QuoteRequest.class);
        logger.info("Received quote request from AI: {}", quoteRequest);
        String template2 = "Now create a quote for the item with id {itemPriceId} in my chargebee account.";
        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(u -> {
                    u.text(template2);
                    u.param("itemPriceId", quoteRequest.itemPriceId());
                })
                .tools(chargebeeApiService)
                .call()
                .content();
        logger.info("Received response from AI: {}", response);
        return response;
    }
}
