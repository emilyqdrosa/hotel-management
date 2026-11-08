package com.redehoteleira.api_hotel.services;

import com.redehoteleira.api_hotel.dtos.HotelRequestDTO;
import com.redehoteleira.api_hotel.model.entities.Hotel;
import com.redehoteleira.api_hotel.repositories.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Transactional
    public Hotel createHotel(HotelRequestDTO data) {
        if (hotelRepository.existsByName(data.name())) {
            throw new IllegalArgumentException("Já existe um hotel cadastrado com este nome.");
        }

        Hotel hotel = new Hotel();
        hotel.setName(data.name());
        hotel.setAddress(data.address());
        hotel.setPhone(data.phone());

        return hotelRepository.save(hotel);
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Hotel getHotelById(UUID id) {
        return hotelRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Hotel não encontrado."));
    }

    @Transactional
    public Hotel updateHotel(UUID id, HotelRequestDTO data) {
        Hotel hotel = getHotelById(id);

        if (!hotel.getName().equals(data.name()) && hotelRepository.existsByName(data.name())) {
            throw new IllegalArgumentException("Já existe outro hotel cadastrado com este nome.");
        }

        hotel.setName(data.name());
        hotel.setAddress(data.address());
        hotel.setPhone(data.phone());

        return hotelRepository.save(hotel);
    }

    @Transactional
    public void deleteHotel(UUID id) {
        Hotel hotel = getHotelById(id);
        hotelRepository.delete(hotel);
    }
}