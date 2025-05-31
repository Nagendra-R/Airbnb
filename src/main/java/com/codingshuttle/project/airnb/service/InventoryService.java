package com.codingshuttle.project.airnb.service;

import com.codingshuttle.project.airnb.dto.HotelDto;
import com.codingshuttle.project.airnb.dto.HotelPriceDto;
import com.codingshuttle.project.airnb.dto.HotelSearchRequest;
import com.codingshuttle.project.airnb.entites.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto>  searchHotels(HotelSearchRequest hotelSearchRequest);
}
