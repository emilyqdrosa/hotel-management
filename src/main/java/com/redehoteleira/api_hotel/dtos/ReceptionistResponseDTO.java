package com.redehoteleira.api_hotel.dtos;

import java.time.LocalDate;
import java.util.UUID;

public record ReceptionistResponseDTO(
        UUID id_employee,
        UUID id_hotel,
        String name,
        String cpf,
        Double salary,
        LocalDate hireDate,
        UUID id_work_shift,
        Boolean isOnVacation,
        String spokenLanguages
) {
}