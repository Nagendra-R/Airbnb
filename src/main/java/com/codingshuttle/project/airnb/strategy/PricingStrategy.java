package com.codingshuttle.project.airnb.strategy;

import com.codingshuttle.project.airnb.entites.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);

}
