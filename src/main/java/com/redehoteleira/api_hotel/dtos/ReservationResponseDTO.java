package com.redehoteleira.api_hotel.dtos;

import com.redehoteleira.api_hotel.model.enums.ReservationStatus;
import com.redehoteleira.api_hotel.model.enums.ServiceType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReservationResponseDTO(
        UUID id_reservation,
        UUID id_guest,
        UUID id_room,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Double totalValue,
        ReservationStatus status,
        List<ServiceType> consumedServices
) {
}