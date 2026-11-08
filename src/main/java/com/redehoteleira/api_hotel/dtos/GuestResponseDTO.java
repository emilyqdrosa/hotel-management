package com.redehoteleira.api_hotel.dtos;

import java.time.LocalDate;
import java.util.UUID;

public record GuestResponseDTO(
        UUID id_guest,
        String name,
        String cpf,
        String email,
        String phoneNumber,
        LocalDate birthDate
) {
}