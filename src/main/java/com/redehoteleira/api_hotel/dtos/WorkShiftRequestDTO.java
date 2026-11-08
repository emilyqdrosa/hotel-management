package com.redehoteleira.api_hotel.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record WorkShiftRequestDTO(

        @NotBlank(message = "A descrição do turno é obrigatória.")
        @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres.")
        String description,

        @NotNull(message = "O horário de início é obrigatório.")
        LocalTime startTime,

        @NotNull(message = "O horário de término é obrigatório.")
        LocalTime endTime,

        @NotBlank(message = "Os dias de trabalho são obrigatórios.")
        String workingDays

) {
}