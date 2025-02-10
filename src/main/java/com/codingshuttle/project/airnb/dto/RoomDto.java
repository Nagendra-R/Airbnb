package com.codingshuttle.project.airnb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto {

    private Long id;
    private String name;
    private String type;
    private BigDecimal basePrice;
    private String[] photos;
    private String[] amenities;
    //total no of rooms
    private Integer totalCount;
    private Integer capacity;           //2,3,4


}
