package com.codingshuttle.project.airnb.dto;


import com.codingshuttle.project.airnb.entites.Hotel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class HotelPriceDto {

    private Hotel hotel;
    private Double price;

    public HotelPriceDto(Hotel hotel, Double price) {
        this.hotel = hotel;
        this.price = price;
    }
}
