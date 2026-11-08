package com.redehoteleira.api_hotel.dtos;

import com.redehoteleira.api_hotel.model.enums.ServiceType;

import java.util.UUID;

public record ServiceResponseDTO(
        UUID id_service,
        ServiceType type,
        String name,
        Double price
) {
}