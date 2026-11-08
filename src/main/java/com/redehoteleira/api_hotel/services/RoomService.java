package com.redehoteleira.api_hotel.services;

import com.redehoteleira.api_hotel.dtos.LuxuryRoomRequestDTO;
import com.redehoteleira.api_hotel.dtos.RoomRequestDTO;
import com.redehoteleira.api_hotel.dtos.StandardRoomRequestDTO;
import com.redehoteleira.api_hotel.model.entities.Hotel;
import com.redehoteleira.api_hotel.model.entities.LuxuryRoom;
import com.redehoteleira.api_hotel.model.entities.Room;
import com.redehoteleira.api_hotel.model.entities.StandardRoom;
import com.redehoteleira.api_hotel.repositories.HotelRepository;
import com.redehoteleira.api_hotel.repositories.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    @Transactional
    public StandardRoom createStandardRoom(RoomRequestDTO baseData, StandardRoomRequestDTO specificData) {
        Hotel hotel = getHotel(baseData.id_hotel());
        validateRoomNumber(baseData.number(), hotel);

        StandardRoom room = new StandardRoom(
                null, hotel, baseData.number(), baseData.capacity(),
                baseData.dailyRate(), baseData.status(), specificData.hasAirConditioning()
        );

        return roomRepository.save(room);
    }

    @Transactional
    public LuxuryRoom createLuxuryRoom(RoomRequestDTO baseData, LuxuryRoomRequestDTO specificData) {
        Hotel hotel = getHotel(baseData.id_hotel());
        validateRoomNumber(baseData.number(), hotel);

        LuxuryRoom room = new LuxuryRoom(
                null, hotel, baseData.number(), baseData.capacity(),
                baseData.dailyRate(), baseData.status(), specificData.hasJacuzzi()
        );

        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quarto não encontrado."));
    }

    @Transactional
    public StandardRoom updateStandardRoom(UUID id, RoomRequestDTO baseData, StandardRoomRequestDTO specificData) {
        Room room = getRoomById(id);

        if (!(room instanceof StandardRoom)) {
            throw new IllegalArgumentException("O quarto informado não é do tipo Standard.");
        }

        Hotel hotel = getHotel(baseData.id_hotel());
        validateNumberForUpdate(room, baseData.number(), hotel);

        StandardRoom standardRoom = (StandardRoom) room;
        updateBaseRoomData(standardRoom, baseData, hotel);
        standardRoom.setHasAirConditioning(specificData.hasAirConditioning());

        return roomRepository.save(standardRoom);
    }

    @Transactional
    public LuxuryRoom updateLuxuryRoom(UUID id, RoomRequestDTO baseData, LuxuryRoomRequestDTO specificData) {
        Room room = getRoomById(id);

        if (!(room instanceof LuxuryRoom)) {
            throw new IllegalArgumentException("O quarto informado não é do tipo Luxo.");
        }

        Hotel hotel = getHotel(baseData.id_hotel());
        validateNumberForUpdate(room, baseData.number(), hotel);

        LuxuryRoom luxuryRoom = (LuxuryRoom) room;
        updateBaseRoomData(luxuryRoom, baseData, hotel);
        luxuryRoom.setHasJacuzzi(specificData.hasJacuzzi());

        return roomRepository.save(luxuryRoom);
    }

    @Transactional
    public void deleteRoom(UUID id) {
        Room room = getRoomById(id);
        roomRepository.delete(room);
    }

    private Hotel getHotel(UUID hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Hotel não encontrado."));
    }

    private void validateRoomNumber(String number, Hotel hotel) {
        if (roomRepository.existsByNumberAndHotel(number, hotel)) {
            throw new IllegalArgumentException("Já existe um quarto cadastrado com este número neste hotel.");
        }
    }

    private void validateNumberForUpdate(Room room, String newNumber, Hotel hotel) {
        boolean isDifferentNumber = !room.getNumber().equals(newNumber);
        boolean isDifferentHotel = !room.getHotel().getId().equals(hotel.getId());

        if ((isDifferentNumber || isDifferentHotel) && roomRepository.existsByNumberAndHotel(newNumber, hotel)) {
            throw new IllegalArgumentException("Já existe outro quarto cadastrado com este número neste hotel.");
        }
    }

    private void updateBaseRoomData(Room room, RoomRequestDTO baseData, Hotel hotel) {
        room.setHotel(hotel);
        room.setNumber(baseData.number());
        room.setCapacity(baseData.capacity());
        room.setDailyRate(baseData.dailyRate());
        room.setStatus(baseData.status());
    }
}