package com.redehoteleira.api_hotel.dtos;

import java.util.UUID;

public record HotelResponseDTO(
        UUID id_hotel,
        String name,
        String address,
        String phone
) {
}