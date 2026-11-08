package com.redehoteleira.api_hotel.dtos;

import com.redehoteleira.api_hotel.model.enums.SystemAccessLevel;

import java.time.LocalDate;
import java.util.UUID;

public record AdministratorResponseDTO(
        UUID id_employee,
        UUID id_hotel,
        String name,
        String cpf,
        Double salary,
        LocalDate hireDate,
        UUID id_work_shift,
        Boolean isOnVacation,
        SystemAccessLevel systemAccessLevel
) {
}