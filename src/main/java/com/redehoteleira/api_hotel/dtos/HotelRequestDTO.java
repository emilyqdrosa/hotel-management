package com.redehoteleira.api_hotel.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record HotelRequestDTO(

        @NotBlank(message = "O nome do hotel é obrigatório")
        @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres")
        String name,

        @NotBlank(message = "O endereço do hotel é obrigatório")
        String address,

        @NotBlank(message = "O telefone do hotel é obrigatório")
        @Pattern(regexp = "^[0-9()+\\-\\s]{10,20}$", message = "Telefone inválido")
        String phone

) {
}