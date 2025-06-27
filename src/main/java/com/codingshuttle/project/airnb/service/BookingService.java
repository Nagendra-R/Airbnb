package com.codingshuttle.project.airnb.service;

import com.codingshuttle.project.airnb.dto.BookingDto;
import com.codingshuttle.project.airnb.dto.BookingRequest;
import com.codingshuttle.project.airnb.dto.GuestDto;
import com.stripe.model.Event;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);
}
