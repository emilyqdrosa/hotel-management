package com.redehoteleira.api_hotel.model.entities;

import com.redehoteleira.api_hotel.model.enums.RoomStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "luxury_rooms")
@PrimaryKeyJoinColumn(name = "id_room")
public class LuxuryRoom extends Room {

    @Column(name = "has_jacuzzi")
    private Boolean hasJacuzzi;

    public LuxuryRoom() {
        super();
    }

    public LuxuryRoom(UUID id_room, Hotel hotel, String number, Integer capacity, Double dailyRate, RoomStatus status, Boolean hasJacuzzi) {
        super(id_room, hotel, number, capacity, dailyRate, status);
        this.hasJacuzzi = hasJacuzzi;
    }

    @Override
    public Double calculateTotalRate(Integer days) {
        Double baseRate = this.getDailyRate() * days;
        return baseRate + (baseRate * 0.50);
    }

    public Boolean getHasJacuzzi() {
        return hasJacuzzi;
    }

    public void setHasJacuzzi(Boolean hasJacuzzi) {
        this.hasJacuzzi = hasJacuzzi;
    }
}
