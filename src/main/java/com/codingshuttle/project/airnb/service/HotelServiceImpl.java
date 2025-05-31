package com.codingshuttle.project.airnb.service;

import com.codingshuttle.project.airnb.dto.HotelDto;
import com.codingshuttle.project.airnb.dto.HotelInfoDto;
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
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;


    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("creating new hotel with name {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);

        return modelMapper.map(hotelRepository.save(hotel), HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with Id: " + id));
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long hotelId, HotelDto hotelDto) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with hotel id :" + hotelId));
        hotelDto.setActive(false);
        modelMapper.map(hotelDto, hotel);
        hotel.setId(hotelId);
        Hotel updateHotel = hotelRepository.save(hotel);
        return modelMapper.map(updateHotel, HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with hotel id :" + hotelId));

        //TODO:: delete future inventories for this hotel
        List<Room> rooms = hotel.getRooms();
        for (Room room : rooms) {
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }

        hotelRepository.deleteById(hotelId);
    }

    @Override
    @Transactional
    public HotelDto activateHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with hotel id :" + hotelId));
        hotel.setActive(true);
        Hotel activatedHotel = hotelRepository.save(hotel);
        log.info("activated the hotel with id : {}", hotelId);

        //TODO:: create inventory for all the rooms
        //crate inventory for all rooms in this hotel
        //assuming only first time
        List<Room> rooms = hotel.getRooms();
        for (Room room : rooms) {
            inventoryService.initializeRoomForAYear(room);
        }

        return modelMapper.map(activatedHotel, HotelDto.class);
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with hotel id :" + hotelId));
        List<RoomDto> rooms = hotel.getRooms().stream().
                map((element) -> modelMapper.map(element, RoomDto.class))
                .toList();

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);

    }

}
