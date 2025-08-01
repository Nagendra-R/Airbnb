package com.codingshuttle.project.airnb.service;

import com.codingshuttle.project.airnb.entites.Hotel;
import com.codingshuttle.project.airnb.entites.HotelMinPrice;
import com.codingshuttle.project.airnb.entites.Inventory;
import com.codingshuttle.project.airnb.repository.HotelMinPriceRepository;
import com.codingshuttle.project.airnb.repository.HotelRepository;
import com.codingshuttle.project.airnb.repository.InventoryRepository;
import com.codingshuttle.project.airnb.strategy.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

    //scheduler to add update the inventory and hotelMinPrice tables for an every hour

    @Scheduled(cron = "0 */50 * * * *")
//    @Scheduled(cron = "0 0 * * * *")
    public void updatePrices() {
        log.info("Updating prices for every 5 seconds");
        int page = 0;
        int pageSize = 100;
        while (true) {
            Page<Hotel> hotels = hotelRepository.findAll(PageRequest.of(page, pageSize));
            if (hotels.isEmpty()) {
                break;
            }
            hotels.getContent().forEach(hotel -> updateHotelPrices(hotel));
            page++;
        }
    }

    private void updateHotelPrices(Hotel hotel) {
        log.info("Update the hotel prices for {}",hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);
        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);

        updateInventoryPrices(inventoryList);
        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        //compute minimum price per day for the hotel
        Map<LocalDate,BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice , Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey , e -> e.getValue().orElse(BigDecimal.ZERO)));

        List<HotelMinPrice> hotelMinPrices = new ArrayList<>();
        dailyMinPrices.forEach((date,price)->{
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                    .orElse(new HotelMinPrice(hotel,date));
            hotelPrice.setPrice(price);
            hotelMinPrices.add(hotelPrice);
        });

        //save all HotelPrice entities in bulk
        hotelMinPriceRepository.saveAll(hotelMinPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList){
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = pricingService.calculateDynamicPrice(inventory);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }



}
