package com.codingshuttle.project.airnb.dto;


import com.codingshuttle.project.airnb.entites.Guest;
import com.codingshuttle.project.airnb.entites.Hotel;
import com.codingshuttle.project.airnb.entites.Room;
import com.codingshuttle.project.airnb.entites.User;
import com.codingshuttle.project.airnb.entites.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class BookingDto {

    private Long id;
//    private Hotel hotel;
//    private Room room;
//    private User user;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutTime;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
    private BigDecimal amount;
}
