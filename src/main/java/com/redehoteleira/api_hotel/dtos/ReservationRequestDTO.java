package com.redehoteleira.api_hotel.dtos;

import com.redehoteleira.api_hotel.model.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReservationRequestDTO(

        @NotNull(message = "O ID do hóspede é obrigatório.")
        UUID id_guest,

        @NotNull(message = "O ID do quarto é obrigatório.")
        UUID id_room,

        @NotNull(message = "A data de check-in é obrigatória.")
        @FutureOrPresent(message = "A data de check-in não pode ser passada.")
        LocalDate checkInDate,

        @NotNull(message = "A data de check-out é obrigatória.")
        LocalDate checkOutDate,

        @NotNull(message = "O status da reserva é obrigatório.")
        ReservationStatus status,

        List<UUID> consumedServiceIds
) {}