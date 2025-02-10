package com.codingshuttle.project.airnb.service;

import com.codingshuttle.project.airnb.dto.RoomDto;

import java.util.List;

public interface RoomService {

    RoomDto createNewRoom(Long hotelId,RoomDto roomDto);

    List<RoomDto> getAllRoomsInHotel(Long hotelId);

    RoomDto getRoomById(Long hotelId,Long roomId);

    void deleteRoomById(Long hotelId,Long roomId);

}
