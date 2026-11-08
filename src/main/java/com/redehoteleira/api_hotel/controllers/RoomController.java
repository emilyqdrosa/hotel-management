package com.redehoteleira.api_hotel.controllers;

import com.redehoteleira.api_hotel.dtos.LuxuryRoomRequestDTO;
import com.redehoteleira.api_hotel.dtos.LuxuryRoomResponseDTO;
import com.redehoteleira.api_hotel.dtos.RoomRequestDTO;
import com.redehoteleira.api_hotel.dtos.RoomResponseDTO;
import com.redehoteleira.api_hotel.dtos.StandardRoomRequestDTO;
import com.redehoteleira.api_hotel.dtos.StandardRoomResponseDTO;
import com.redehoteleira.api_hotel.model.entities.LuxuryRoom;
import com.redehoteleira.api_hotel.model.entities.Room;
import com.redehoteleira.api_hotel.model.entities.StandardRoom;
import com.redehoteleira.api_hotel.services.RoomService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    public record StandardRoomPayload(
            @NotNull(message = "Os dados do quarto são obrigatórios") @Valid RoomRequestDTO room,
            @NotNull(message = "Os dados do quarto standard são obrigatórios") @Valid StandardRoomRequestDTO details
    ) {}

    public record LuxuryRoomPayload(
            @NotNull(message = "Os dados do quarto são obrigatórios") @Valid RoomRequestDTO room,
            @NotNull(message = "Os dados do quarto luxo são obrigatórios") @Valid LuxuryRoomRequestDTO details
    ) {}

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/standard")
    public ResponseEntity<StandardRoomResponseDTO> createStandard(@RequestBody @Valid StandardRoomPayload payload) {
        StandardRoom room = roomService.createStandardRoom(payload.room(), payload.details());
        return ResponseEntity.status(HttpStatus.CREATED).body(toStandardResponse(room));
    }

    @PostMapping("/luxury")
    public ResponseEntity<LuxuryRoomResponseDTO> createLuxury(@RequestBody @Valid LuxuryRoomPayload payload) {
        LuxuryRoom room = roomService.createLuxuryRoom(payload.room(), payload.details());
        return ResponseEntity.status(HttpStatus.CREATED).body(toLuxuryResponse(room));
    }

    @GetMapping
    public ResponseEntity<List<RoomResponseDTO>> findAll(@RequestParam(required = false) UUID hotelId,
                                                         @RequestParam(required = false) Boolean available) {
        List<RoomResponseDTO> rooms = roomService.getAllRooms()
                .stream()
                .filter(r -> hotelId == null || r.getHotel().getId().equals(hotelId))
                .filter(r -> available == null || r.isAvailable() == available)
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable UUID id) {
        Room room = roomService.getRoomById(id);

        if (room instanceof StandardRoom standardRoom) {
            return ResponseEntity.ok(toStandardResponse(standardRoom));
        }
        if (room instanceof LuxuryRoom luxuryRoom) {
            return ResponseEntity.ok(toLuxuryResponse(luxuryRoom));
        }
        return ResponseEntity.ok(toResponse(room));
    }

    @PutMapping("/standard/{id}")
    public ResponseEntity<StandardRoomResponseDTO> updateStandard(@PathVariable UUID id,
                                                                  @RequestBody @Valid StandardRoomPayload payload) {
        StandardRoom room = roomService.updateStandardRoom(id, payload.room(), payload.details());
        return ResponseEntity.ok(toStandardResponse(room));
    }

    @PutMapping("/luxury/{id}")
    public ResponseEntity<LuxuryRoomResponseDTO> updateLuxury(@PathVariable UUID id,
                                                              @RequestBody @Valid LuxuryRoomPayload payload) {
        LuxuryRoom room = roomService.updateLuxuryRoom(id, payload.room(), payload.details());
        return ResponseEntity.ok(toLuxuryResponse(room));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    private RoomResponseDTO toResponse(Room room) {
        String type = (room instanceof LuxuryRoom) ? "LUXURY" : "STANDARD";
        return new RoomResponseDTO(
                room.getId_room(),
                room.getNumber(),
                room.getCapacity(),
                room.getDailyRate(),
                room.getStatus(),
                room.getHotel().getId(),
                type
        );
    }

    private StandardRoomResponseDTO toStandardResponse(StandardRoom room) {
        return new StandardRoomResponseDTO(
                room.getId_room(),
                room.getNumber(),
                room.getCapacity(),
                room.getDailyRate(),
                room.getStatus(),
                room.getHotel().getId(),
                room.getHasAirConditioning()
        );
    }

    private LuxuryRoomResponseDTO toLuxuryResponse(LuxuryRoom room) {
        return new LuxuryRoomResponseDTO(
                room.getId_room(),
                room.getNumber(),
                room.getCapacity(),
                room.getDailyRate(),
                room.getStatus(),
                room.getHotel().getId(),
                room.getHasJacuzzi()
        );
    }
}