package com.redehoteleira.api_hotel.dtos;

import com.redehoteleira.api_hotel.model.enums.RoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RoomRequestDTO(

        @NotBlank(message = "O número do quarto é obrigatório")
        @Size(max = 20, message = "O número deve ter no máximo 20 caracteres")
        String number,

        @NotNull(message = "A capacidade do quarto é obrigatória")
        @Positive(message = "A capacidade deve ser maior que zero")
        Integer capacity,

        @NotNull(message = "O valor da diária é obrigatório")
        @Positive(message = "O valor da diária deve ser maior que zero")
        Double dailyRate,

        @NotNull(message = "O status do quarto é obrigatório")
        RoomStatus status,

        @NotNull(message = "O ID do hotel é obrigatório")
        java.util.UUID id_hotel
) {}