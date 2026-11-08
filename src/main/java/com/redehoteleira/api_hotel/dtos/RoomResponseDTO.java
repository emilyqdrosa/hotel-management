package com.redehoteleira.api_hotel.dtos;

import com.redehoteleira.api_hotel.model.enums.RoomStatus;
import java.util.UUID;

public record RoomResponseDTO(
        UUID id_room,
        String number,
        Integer capacity,
        Double dailyRate,
        RoomStatus status,
        UUID id_hotel,
        String roomType
) {
}