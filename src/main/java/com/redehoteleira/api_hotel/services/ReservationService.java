package com.redehoteleira.api_hotel.services;

import com.redehoteleira.api_hotel.dtos.ReservationRequestDTO;
import com.redehoteleira.api_hotel.model.entities.Guest;
import com.redehoteleira.api_hotel.model.entities.Reservation;
import com.redehoteleira.api_hotel.model.entities.Room;
import com.redehoteleira.api_hotel.model.enums.ServiceType;
import com.redehoteleira.api_hotel.repositories.GuestRepository;
import com.redehoteleira.api_hotel.repositories.ReservationRepository;
import com.redehoteleira.api_hotel.repositories.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;

    public ReservationService(ReservationRepository reservationRepository, GuestRepository guestRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public Reservation createReservation(ReservationRequestDTO data) {
        if (data.checkInDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("A data de check-in não pode ser passada.");
        }

        if (data.checkInDate().isAfter(data.checkOutDate()) || data.checkInDate().isEqual(data.checkOutDate())) {
            throw new IllegalArgumentException("A data de check-out deve ser posterior à data de check-in.");
        }

        if (reservationRepository.existsConflictingReservation(data.id_room(), data.checkInDate(), data.checkOutDate())) {
            throw new IllegalArgumentException("O quarto não está disponível para o período selecionado.");
        }

        Guest guest = guestRepository.findById(data.id_guest())
                .orElseThrow(() -> new IllegalArgumentException("Hóspede não encontrado."));

        Room room = roomRepository.findById(data.id_room())
                .orElseThrow(() -> new IllegalArgumentException("Quarto não encontrado."));

        Reservation reservation = new Reservation();
        reservation.setGuest(guest);
        reservation.setRoom(room);
        reservation.setCheckInDate(data.checkInDate());
        reservation.setCheckOutDate(data.checkOutDate());
        reservation.setStatus(data.status());

        if (data.consumedServices() != null) {
            reservation.setConsumedServices(data.consumedServices());
        }

        reservation.calculateTotalValue();
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservationById(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));
    }

    @Transactional
    public Reservation updateReservation(UUID id, ReservationRequestDTO data) {
        Reservation reservation = getReservationById(id);

        if (data.checkInDate().isAfter(data.checkOutDate()) || data.checkInDate().isEqual(data.checkOutDate())) {
            throw new IllegalArgumentException("A data de check-out deve ser posterior à data de check-in.");
        }

        boolean roomChanged = !reservation.getRoom().getId_room().equals(data.id_room());
        boolean datesChanged = !reservation.getCheckInDate().equals(data.checkInDate()) || !reservation.getCheckOutDate().equals(data.checkOutDate());

        if ((roomChanged || datesChanged) && reservationRepository.existsConflictingReservation(data.id_room(), data.checkInDate(), data.checkOutDate())) {
            throw new IllegalArgumentException("O quarto não está disponível para o novo período selecionado.");
        }

        Guest guest = guestRepository.findById(data.id_guest())
                .orElseThrow(() -> new IllegalArgumentException("Hóspede não encontrado."));
        Room room = roomRepository.findById(data.id_room())
                .orElseThrow(() -> new IllegalArgumentException("Quarto não encontrado."));

        reservation.setGuest(guest);
        reservation.setRoom(room);
        reservation.setCheckInDate(data.checkInDate());
        reservation.setCheckOutDate(data.checkOutDate());
        reservation.setStatus(data.status());

        if (data.consumedServices() != null) {
            reservation.setConsumedServices(data.consumedServices());
        } else {
            reservation.getConsumedServices().clear();
        }

        reservation.calculateTotalValue();
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation performCheckIn(UUID id) {
        Reservation reservation = getReservationById(id);
        reservation.performCheckIn();
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation performCheckOut(UUID id) {
        Reservation reservation = getReservationById(id);
        reservation.performCheckOut();
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(UUID id) {
        Reservation reservation = getReservationById(id);
        reservation.cancelReservation();
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation addServiceToReservation(UUID reservationId, ServiceType serviceType) {
        Reservation reservation = getReservationById(reservationId);
        reservation.addService(serviceType);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void deleteReservation(UUID id) {
        Reservation reservation = getReservationById(id);
        reservationRepository.delete(reservation);
    }
}