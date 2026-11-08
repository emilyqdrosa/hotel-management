package com.redehoteleira.api_hotel.dtos;

import java.util.UUID;
import com.redehoteleira.api_hotel.model.enums.RoomStatus;

public record StandardRoomResponseDTO(
        UUID id_room,
        String number,
        Integer capacity,
        Double dailyRate,
        RoomStatus status,
        UUID id_hotel,
        Boolean hasAirConditioning
) {
}