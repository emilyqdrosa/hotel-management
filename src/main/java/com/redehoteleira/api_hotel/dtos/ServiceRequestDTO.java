package com.redehoteleira.api_hotel.dtos;

import com.redehoteleira.api_hotel.model.enums.ServiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceRequestDTO(
        @NotNull(message = "O tipo do serviço é obrigatório.")
        ServiceType type,

        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String name

) {
}