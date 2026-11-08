package com.redehoteleira.api_hotel.controllers;

import com.redehoteleira.api_hotel.dtos.ReservationRequestDTO;
import com.redehoteleira.api_hotel.dtos.ReservationResponseDTO;
import com.redehoteleira.api_hotel.model.entities.Reservation;
import com.redehoteleira.api_hotel.model.enums.ReservationStatus;
import com.redehoteleira.api_hotel.model.enums.ServiceType;
import com.redehoteleira.api_hotel.services.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> create(@RequestBody @Valid ReservationRequestDTO data) {
        Reservation reservation = reservationService.createReservation(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(reservation));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> findAll(@RequestParam(required = false) UUID guestId,
                                                                @RequestParam(required = false) UUID roomId,
                                                                @RequestParam(required = false) ReservationStatus status) {
        List<ReservationResponseDTO> reservations = reservationService.getAllReservations()
                .stream()
                .filter(r -> guestId == null || r.getGuest().getId_guest().equals(guestId))
                .filter(r -> roomId == null || r.getRoom().getId_room().equals(roomId))
                .filter(r -> status == null || r.getStatus() == status)
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(reservationService.getReservationById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> update(@PathVariable UUID id,
                                                         @RequestBody @Valid ReservationRequestDTO data) {
        return ResponseEntity.ok(toResponse(reservationService.updateReservation(id, data)));
    }

    @PatchMapping("/{id}/check-in")
    public ResponseEntity<ReservationResponseDTO> checkIn(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(reservationService.performCheckIn(id)));
    }

    @PatchMapping("/{id}/check-out")
    public ResponseEntity<ReservationResponseDTO> checkOut(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(reservationService.performCheckOut(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(reservationService.cancelReservation(id)));
    }

    @PostMapping("/{id}/services/{serviceType}")
    public ResponseEntity<ReservationResponseDTO> addService(@PathVariable UUID id,
                                                             @PathVariable ServiceType serviceType) {
        return ResponseEntity.ok(toResponse(reservationService.addServiceToReservation(id, serviceType)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    private ReservationResponseDTO toResponse(Reservation r) {
        return new ReservationResponseDTO(
                r.getId_reservation(),
                r.getGuest().getId_guest(),
                r.getRoom().getId_room(),
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getTotalValue(),
                r.getStatus(),
                List.copyOf(r.getConsumedServices())
        );
    }
}