package com.redehoteleira.api_hotel.dtos;

import java.time.LocalTime;
import java.util.UUID;

public record WorkShiftResponseDTO(
        UUID id_work_shift,
        String description,
        LocalTime startTime,
        LocalTime endTime,
        String workingDays
) {
}