package com.ayush.learn.ai_beginner.model;

public enum PricingModel {
    SUBSCRIPTION("Subscription"),
    ONE_TIME("One-Time Purchase"),
    FREEMIUM("Freemium"),
    PAY_AS_YOU_GO("Pay-As-You-Go"),
    TIERED("Tiered Pricing"),
    VOLUME_BASED("Volume-Based Pricing"),
    DYNAMIC("Dynamic Pricing");

    private final String modelName;

    PricingModel(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }
}
