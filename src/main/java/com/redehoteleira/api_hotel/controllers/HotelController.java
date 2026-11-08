package com.redehoteleira.api_hotel.controllers;

import com.redehoteleira.api_hotel.dtos.HotelRequestDTO;
import com.redehoteleira.api_hotel.dtos.HotelResponseDTO;
import com.redehoteleira.api_hotel.model.entities.Hotel;
import com.redehoteleira.api_hotel.services.HotelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @PostMapping
    public ResponseEntity<HotelResponseDTO> create(@RequestBody @Valid HotelRequestDTO data) {
        Hotel hotel = hotelService.createHotel(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(hotel));
    }

    @GetMapping
    public ResponseEntity<List<HotelResponseDTO>> findAll() {
        List<HotelResponseDTO> hotels = hotelService.getAllHotels()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(hotelService.getHotelById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> update(@PathVariable UUID id,
                                                   @RequestBody @Valid HotelRequestDTO data) {
        return ResponseEntity.ok(toResponse(hotelService.updateHotel(id, data)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    private HotelResponseDTO toResponse(Hotel hotel) {
        return new HotelResponseDTO(
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getPhone()
        );
    }
}