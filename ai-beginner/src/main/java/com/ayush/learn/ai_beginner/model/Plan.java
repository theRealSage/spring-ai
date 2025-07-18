package com.ayush.learn.ai_beginner.model;

public record Plan(String planName, Double price, String currency, PricingModel pricingModel, String entitlements) {
}
