package com.codingshuttle.project.airnb.strategy;

import com.codingshuttle.project.airnb.entites.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    PricingStrategy pricingStrategy = new BasePricingStrategy();

    public BigDecimal calculateDynamicPrice(Inventory inventory){
        PricingStrategy pricingStrategy = new BasePricingStrategy();

        //apply all additional strategies
        pricingStrategy = new SurgePricingStrategy(pricingStrategy);
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
//        pricingStrategy = new HolidayPricingStrategy(pricingStrategy);

        BigDecimal dynamicPrice = pricingStrategy.calculatePrice(inventory);
        return dynamicPrice;
    }

}
