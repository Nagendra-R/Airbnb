package com.codingshuttle.project.airnb.strategy;

import com.codingshuttle.project.airnb.entites.Inventory;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
public class UrgencyPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        LocalDate localDate = LocalDate.now();
        BigDecimal price = inventory.getPrice();
        if (!inventory.getDate().isBefore(localDate) && inventory.getDate().isBefore(localDate.plusDays(7))){
            return price.multiply(BigDecimal.valueOf(1.15));
        }
        return price;
    }
}
