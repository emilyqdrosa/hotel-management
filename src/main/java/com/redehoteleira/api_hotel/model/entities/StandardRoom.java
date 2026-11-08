package com.redehoteleira.api_hotel.model.entities;

import com.redehoteleira.api_hotel.model.enums.RoomStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "standard_rooms")
@PrimaryKeyJoinColumn(name = "id_room")
public class StandardRoom extends Room {

    @Column(name = "has_air_conditioning")
    private Boolean hasAirConditioning;

    public StandardRoom() {
        super();
    }

    public StandardRoom(UUID id_room, Hotel hotel, String number, Integer capacity, Double dailyRate, RoomStatus status, Boolean hasAirConditioning) {
        super(id_room, hotel, number, capacity, dailyRate, status);
        this.hasAirConditioning = hasAirConditioning;
    }

    @Override
    public Double calculateTotalRate(Integer days) {
        return this.getDailyRate() * days;
    }

    public Boolean getHasAirConditioning() {
        return hasAirConditioning;
    }

    public void setHasAirConditioning(Boolean hasAirConditioning) {
        this.hasAirConditioning = hasAirConditioning;
    }
}
