package com.codingshuttle.project.airnb.service;

import com.codingshuttle.project.airnb.dto.RoomDto;
import com.codingshuttle.project.airnb.entites.Hotel;
import com.codingshuttle.project.airnb.entites.Room;
import com.codingshuttle.project.airnb.exception.ResourceNotFoundException;
import com.codingshuttle.project.airnb.repository.HotelRepository;
import com.codingshuttle.project.airnb.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("creating new room in hotel id:{} with roomDto {}", hotelId, roomDto);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with hotelId" + hotelId));

        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

//        TODO1:: create a inventory for next one year as soon as room is created and hotel is active
        if(hotel.getActive()){
            log.info("creating an inventory");
            inventoryService.initializeRoomForAYear(room);
        }

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with hotelId" + hotelId));

        List<Room> rooms = hotel.getRooms();
        return rooms.stream()
                .map((room) -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long hotelId, Long roomId) {
        log.info("get room by room id: {}", roomId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with hotelId" + hotelId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("room not found wih roomId: " + roomId));
        return modelMapper.map(room, RoomDto.class);

        //          chatgpt // check the room from the respective hotel only
//        if(!room.getHotel().getId().equals(hotelId)){
//             throw new ResourceNotFoundException("room with id: "+roomId +"not found for hotel with id "+ hotelId);
//        }
    }

    @Override
    @Transactional
    public void deleteRoomById(Long hotelId, Long roomId) {
        log.info("delete room by id: {}", roomId);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with hotelId" + hotelId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("room not found wih roomId: " + roomId));



        //TODO1:: delete the future inventory for this room
        inventoryService.deleteAllInventories(room);
        roomRepository.delete(room);

        //        chatgpt
//        Hotel hotel = hotelRepository.findById(hotelId)
//                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with hotelId" + hotelId));
//        Room room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new ResourceNotFoundException("room not found wih roomId: " + roomId));
//        if(!room.getHotel().getId().equals(hotelId)){
//             throw new ResourceNotFoundException("room with id: "+roomId +"not found for hotel with id "+ hotelId);
//        }


    }
}
