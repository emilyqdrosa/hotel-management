package com.redehoteleira.api_hotel.dtos;

import jakarta.validation.constraints.NotNull;

public record StandardRoomRequestDTO(

        @NotNull(message = "A informação sobre ar-condicionado é obrigatória")
        Boolean hasAirConditioning
) {
}