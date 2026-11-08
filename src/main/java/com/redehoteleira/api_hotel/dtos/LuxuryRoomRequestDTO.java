package com.redehoteleira.api_hotel.dtos;

import jakarta.validation.constraints.NotNull;

public record LuxuryRoomRequestDTO(

        @NotNull(message = "A informação sobre jacuzzi é obrigatória")
        Boolean hasJacuzzi

) {
}