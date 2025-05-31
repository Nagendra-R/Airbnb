package com.codingshuttle.project.airnb.controller;

import com.codingshuttle.project.airnb.dto.BookingDto;
import com.codingshuttle.project.airnb.dto.BookingRequest;
import com.codingshuttle.project.airnb.dto.GuestDto;
import com.codingshuttle.project.airnb.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/bookings")
@RestController
@Slf4j
public class HotelBookingController {

    private final BookingService bookingService;


    @PostMapping("")
    public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequest bookingRequest){
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
    }


    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId, @RequestBody List<GuestDto> guestDtoList){
        return ResponseEntity.ok(bookingService.addGuests(bookingId,guestDtoList));
    }



}
