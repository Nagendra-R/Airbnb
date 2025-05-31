package com.codingshuttle.project.airnb.service;

import com.codingshuttle.project.airnb.dto.BookingDto;
import com.codingshuttle.project.airnb.dto.BookingRequest;
import com.codingshuttle.project.airnb.dto.GuestDto;
import com.codingshuttle.project.airnb.entites.*;
import com.codingshuttle.project.airnb.entites.enums.BookingStatus;
import com.codingshuttle.project.airnb.exception.ResourceNotFoundException;
import com.codingshuttle.project.airnb.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    public final GuestRepository guestRepository;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {
        log.info("booking initialise with hotel id: {} and room id: {} and check in {} and check out date {}",
                bookingRequest.getHotelId(), bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with hotel id: " + bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("room not found with hotel id: " + bookingRequest.getRoomId()));

//        chatgpt
//        if (!room.getHotel().getId().equals(hotel.getId())) {
//            throw new ResourceNotFoundException("Room does not belong to the specified hotel.");
//        }

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;
        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount(), daysCount);

        if (inventoryList.size() != daysCount) {
            throw new ResourceNotFoundException("Rooms are not available for entire duration");
        }

        // Reserve the room/ update the booked count of inventories
        for (Inventory inventory : inventoryList) {
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
        }
        inventoryRepository.saveAll(inventoryList);

//        User user = new User();//TODO :: Remove dummy user
//        user.setId(1L);

        //create the Booking
        //add dynamic price

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutTime(bookingRequest.getCheckOutDate())
                .roomsCount(bookingRequest.getRoomsCount())
                .user(getCurrentUser())
                .amount(BigDecimal.TEN)
                .build();

        booking = bookingRepository.save(booking);

        log.info("booking is initialized with check in {} and check out date {}"
                , bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests {} to the booking id: {}", guestDtoList, bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        //check bookingStatus is in RESERVED
        if (!booking.getBookingStatus().equals(BookingStatus.RESERVED)) {
            throw new ResourceNotFoundException("Guests can only be added to RESERVED bookings");
        }

        if (hasExpiredBooking(booking)) {
            booking.setBookingStatus(BookingStatus.EXPIRED);
            throw new ResourceNotFoundException("Time add to guests has expired of 10 minutes");
        }

//        for (GuestDto guest: guestDtoList){
//            Guest guests = modelMapper.map(guest,Guest.class);
//            booking.getGuests().add(guests);
//
//        }

        for (GuestDto guestDto :guestDtoList){
            Guest guest = modelMapper.map(guestDto,Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking,BookingDto.class);

    }

    private boolean hasExpiredBooking(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    private User getCurrentUser(){
        User user = userRepository.findById(1L)
                .orElseThrow(()->new ResourceNotFoundException("User Not found with  id :"+1));
        return user;
    }
}
