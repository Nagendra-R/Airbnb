package com.codingshuttle.project.airnb.controller;


import com.codingshuttle.project.airnb.dto.HotelDto;
import com.codingshuttle.project.airnb.dto.HotelInfoDto;
import com.codingshuttle.project.airnb.dto.HotelPriceDto;
import com.codingshuttle.project.airnb.dto.HotelSearchRequest;
import com.codingshuttle.project.airnb.service.HotelService;
import com.codingshuttle.project.airnb.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/hotels")
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @PostMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest) {
        Page<HotelPriceDto> hotels = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }


}
