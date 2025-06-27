package com.codingshuttle.project.airnb.service;

import com.codingshuttle.project.airnb.dto.BookingDto;
import com.codingshuttle.project.airnb.dto.BookingRequest;
import com.codingshuttle.project.airnb.dto.GuestDto;
import com.codingshuttle.project.airnb.entites.*;
import com.codingshuttle.project.airnb.entites.enums.BookingStatus;
import com.codingshuttle.project.airnb.exception.ResourceNotFoundException;
import com.codingshuttle.project.airnb.exception.UnAuthorizedException;
import com.codingshuttle.project.airnb.repository.*;
import com.codingshuttle.project.airnb.strategy.PricingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


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
    private final GuestRepository guestRepository;
    private final PricingService pricingService;
    private final CheckoutService checkoutService;
    private final ObjectMapper objectMapper;
    private static final Gson gson = new Gson();

    @Value("${frontend.url}")
    private String frontendUrl;

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

//        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;
//        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount(), daysCount);
//
//        if (inventoryList.size() != daysCount) {
//            throw new ResourceNotFoundException("Rooms are not available for entire duration");
//        }
//
//        // Reserve the room/ update the booked count of inventories
//        for (Inventory inventory : inventoryList) {
//            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
//        }
//        inventoryRepository.saveAll(inventoryList);

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;
        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount(), daysCount);

        if (inventoryList.size() != daysCount) {
            throw new ResourceNotFoundException("Rooms are not available for entire duration");
        }

        inventoryRepository.initBooking(bookingRequest.getRoomId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());

//        User user = new User();//TODO :: Remove dummy user--- finished !!

        BigDecimal priceOfOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceOfOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

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
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);

        log.info("booking is initialized with check in {} and check out date {}"
                , bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests for booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with id: " + bookingId));
        User user = getCurrentUser();

        log.info("authenicated user: {}", user.hashCode());
        log.info("booking user: {}", booking.getUser().hashCode());

        if (user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: " + user.getId());
        }

        if (hasExpiredBooking(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        if (booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
        }

        for (GuestDto guestDto : guestDtoList) {
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }


    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        User user = getCurrentUser();

//        log.info("authenicated user: {}", user.hashCode());
//        log.info("booking user: {}", booking.getUser().hashCode());

        log.info("Authenticated User ID: {}, class: {}", user.getId(), user.getClass());
        log.info("Booking User ID: {}, class: {}", booking.getUser().getId(), booking.getUser().getClass());
        log.info("Authenticated User equals Booking User? {}", user.equals(booking.getUser()));


        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnAuthorizedException("User with unauthorized details: " + user);
        }

        if (hasExpiredBooking(booking)) {
            booking.setBookingStatus(BookingStatus.EXPIRED);
            throw new IllegalStateException("Booking has expired 10 min in payments. Try again!!..with new Booking ");
        }

        String sessionUrl = checkoutService.getSessionUrl(booking,
                frontendUrl + "/payments/success", frontendUrl + "/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

//    @Override
//    @Transactional
//    public void capturePayment(Event event) {
//        if ("checkout.session.completed".equals(event.getType())) {
//
//            log.info("===============");
//            log.info("== event.getType() === :  {}",event.getType());
//            log.info("===============");
//
//
//            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
//
//            StripeObject stripeObject = deserializer.getObject().orElse(null);
//            if (stripeObject == null) {
//                log.warn("Failed to deserialize checkout.session.completed payload.");
//                return;
//            }
//
//            Session session = (Session) stripeObject;
//            String sessionId = session.getId();
//
////            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
////            if (session == null) return;
////            String sessionId = session.getId();
//            Booking booking = bookingRepository.findByPaymentSessionId(sessionId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found with session id: " + sessionId));
//
//            booking.setBookingStatus(BookingStatus.CONFIRMED);
//            bookingRepository.save(booking);
//
//            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
//                    booking.getCheckOutTime(),booking.getRoomsCount());
//
//            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
//                    booking.getCheckOutTime(),booking.getRoomsCount());
//
//            log.info("Successfully confirmed the booking with id: {}",booking.getId());
//
//        } else {
//            log.warn("Unhandled event type: {}", event.getType());
//        }
//    }

    @Override
    @Transactional
    public void capturePayment(Event event) {

        String eventType = event.getType();
        log.info("Received Stripe webhook event type: {}", eventType);

        if ("checkout.session.completed".equals(eventType)) {
            Session session = retrieveSessionFromEvent(event);
//            ================
//            log.info("session from retrieveSessionFromEvent: {}",retrieveSessionFromEvent(event));
            log.info("Session Details:  {}",session);
            log.info("Session ID Details:  {}",session.getId());
//            ===========?
            if (session.getId() == null) {
                log.info("session id {}",session.getId());
                return;
            }


            Booking booking = bookingRepository.findByPaymentSessionId(session.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found for session ID: " + session.getId()));

            log.info("booking details with sessionPaymentId: {}",booking.getPaymentSessionId());
            booking.setBookingStatus(BookingStatus.CONFIRMED);
             Booking savedBooking = bookingRepository.save(booking);

            log.info("Booking details after saving:: {}",savedBooking);
            log.info(" booking status:: {}",savedBooking.getBookingStatus());

            List<Inventory> inventories = inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutTime(), booking.getRoomsCount());

            log.info("confirmBooking======================");

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutTime(), booking.getRoomsCount());

            log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());

        } else {
            log.warn("Unhandled event type: {}", event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        log.info("CancelBooking for booking id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with id: " + bookingId));
        User user = getCurrentUser();

        log.info("authenicated user: {}", user.hashCode());
        log.info("booking user: {}", booking.getUser().hashCode());

        if (user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: " + user.getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only Confirmed Bookings can be cancelled!!!....");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking canceledBooking = bookingRepository.save(booking);

        log.info("Successfully canceled the Booking status :: {} {}",
                canceledBooking.getBookingStatus(),canceledBooking.getId());

        log.info("Start the Refund Process!!!...");
        List<Inventory> inventories = inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutTime(), booking.getRoomsCount());
        log.info("cancelBooking======================");
        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutTime(), booking.getRoomsCount());
        log.info("Successfully cancelled the booking for Booking ID: {}", booking.getId());

        //initiate the refund

        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund refund = Refund.create(refundCreateParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        log.info("Successfully refund the amount for Booking ID: {}", booking.getId());
    }

    @Override
    public String getBookingStatus(Long bookingId) {
        log.info("get the Booking status for booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with id: " + bookingId));
        User user = getCurrentUser();

        log.info("authenicated user: {}", user.hashCode());
        log.info("booking user: {}", booking.getUser().hashCode());

        if (user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: " + user.getId());
        }

        return booking.getBookingStatus().name();
    }

//        String eventType = event.getType();
//        log.info("Received Stripe webhook event type: {}", eventType);
//
//        if ("checkout.session.completed".equalsIgnoreCase(eventType.trim())) {
//            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
//
//            log.debug("Raw event JSON: {}", deserializer.getRawJson());
//            log.info("Raw payload for event {}: {}", event.getId(), deserializer.getRawJson());
//
//            Session session = null;
//            if (deserializer.getObject().isPresent()) {
//                session = (Session) deserializer.getObject().get();
//            } else {
//                // Fallback if automatic deserialization failed
//                log.warn("Stripe session auto-deserialization failed. Trying manual Gson fallback.");
//                session = gson.fromJson(deserializer.getRawJson(), Session.class);
//            }
//
//            if (session == null) {
//                log.error("Stripe session could not be deserialized. Aborting.");
//                return;
//            }
//
//            log.info("Handling Stripe session ID: {}", session.getId());
//
////            Session session = (Session) event.getDataObjectDeserializer().getObject()
////                    .orElse(null);
////            if (session == null) {
////                log.warn("Stripe session object is null");
////                return;
////            }
//
////            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
////            if (session == null) return;
//
//            String sessionId = session.getId();
//            Booking booking =
//                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(() ->
//                            new ResourceNotFoundException("Booking not found for session ID: "+sessionId));
//
//            booking.setBookingStatus(BookingStatus.CONFIRMED);
//            bookingRepository.save(booking);
//
//            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
//                    booking.getCheckOutTime(), booking.getRoomsCount());
//
//            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
//                    booking.getCheckOutTime(), booking.getRoomsCount());
//
//            log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());
//        } else {
//            log.warn("Unhandled event type: {}", event.getType());
//        }


    private Session retrieveSessionFromEvent(Event event) {
        log.info("inside  retrieveSessionFromEvent");
        try {

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isPresent()) {
                return (Session) deserializer.getObject().get();
            } else {
                String rawJson = event.getData().getObject().toJson();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(rawJson);
                String sessionId = jsonNode.get("id").asText();
                return Session.retrieve(sessionId);
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to retrieve session data");
        }
    }

    private boolean hasExpiredBooking(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}
