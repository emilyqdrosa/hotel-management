package com.redehoteleira.api_hotel.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeRequestDTO(

        @NotNull(message = "O ID do hotel é obrigatório.")
        UUID id_hotel,

        @NotBlank(message = "O nome do funcionário é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @NotBlank(message = "O CPF é obrigatório.")
        @Pattern(
                regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",
                message = "O CPF deve conter 11 dígitos, podendo estar formatado."
        )
        String cpf,

        @NotNull(message = "O salário é obrigatório.")
        @Positive(message = "O salário deve ser maior que zero.")
        Double salary,

        @NotNull(message = "A data de contratação é obrigatória.")
        @PastOrPresent(message = "A data de contratação não pode ser futura.")
        LocalDate hireDate,

        UUID id_work_shift

) {
}