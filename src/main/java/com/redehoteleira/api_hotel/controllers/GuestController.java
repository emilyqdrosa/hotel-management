package com.redehoteleira.api_hotel.controllers;

import com.redehoteleira.api_hotel.dtos.GuestRequestDTO;
import com.redehoteleira.api_hotel.dtos.GuestResponseDTO;
import com.redehoteleira.api_hotel.model.entities.Guest;
import com.redehoteleira.api_hotel.model.entities.Reservation;
import com.redehoteleira.api_hotel.model.enums.ReservationStatus;
import com.redehoteleira.api_hotel.services.GuestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/guests")
public class GuestController {

    public record GuestHistoryDTO(
            UUID id_reservation,
            String hotelName,
            String roomNumber,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            ReservationStatus status,
            List<String> services,
            Double totalValue
    ) {}

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    public ResponseEntity<GuestResponseDTO> create(@RequestBody @Valid GuestRequestDTO data) {
        Guest guest = guestService.createGuest(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(guest));
    }

    @GetMapping
    public ResponseEntity<List<GuestResponseDTO>> findAll(@RequestParam(required = false) String search) {
        String term = search == null ? "" : search.trim().toLowerCase();

        List<GuestResponseDTO> guests = guestService.getAllGuests()
                .stream()
                .filter(g -> term.isEmpty()
                        || g.getName().toLowerCase().contains(term)
                        || g.getCpf().contains(term)
                        || g.getEmail().toLowerCase().contains(term))
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(guests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuestResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(guestService.getGuestById(id)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<GuestHistoryDTO>> history(@PathVariable UUID id) {
        Guest guest = guestService.getGuestById(id);

        List<GuestHistoryDTO> history = guest.getReservations()
                .stream()
                .sorted(Comparator.comparing(Reservation::getCheckInDate).reversed())
                .map(r -> new GuestHistoryDTO(
                        r.getId_reservation(),
                        r.getRoom().getHotel().getName(),
                        r.getRoom().getNumber(),
                        r.getCheckInDate(),
                        r.getCheckOutDate(),
                        r.getStatus(),
                        r.getConsumedServices().stream().map(Enum::name).toList(),
                        r.getTotalValue()
                ))
                .toList();
        return ResponseEntity.ok(history);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuestResponseDTO> update(@PathVariable UUID id,
                                                   @RequestBody @Valid GuestRequestDTO data) {
        return ResponseEntity.ok(toResponse(guestService.updateGuest(id, data)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        guestService.deleteGuest(id);
        return ResponseEntity.noContent().build();
    }

    private GuestResponseDTO toResponse(Guest guest) {
        return new GuestResponseDTO(
                guest.getId_guest(),
                guest.getName(),
                guest.getCpf(),
                guest.getEmail(),
                guest.getPhoneNumber(),
                guest.getBirthDate()
        );
    }
}