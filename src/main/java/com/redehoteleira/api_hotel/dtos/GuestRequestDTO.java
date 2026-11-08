package com.redehoteleira.api_hotel.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record GuestRequestDTO(

        @NotBlank(message = "O nome do hóspede é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @NotBlank(message = "O CPF é obrigatório.")
        @Pattern(
                regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",
                message = "O CPF deve conter exatamente 11 dígitos numéricos."
        )
        String cpf,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado é inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @Pattern(
                regexp = "^[0-9()+\\-\\s]{10,20}$",
                message = "O telefone deve conter entre 10 e 20 caracteres válidos."
        )
        String phoneNumber,

        @Past(message = "A data de nascimento deve ser anterior à data atual.")
        LocalDate birthDate

) {
}