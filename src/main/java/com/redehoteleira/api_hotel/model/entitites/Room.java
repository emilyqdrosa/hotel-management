package com.redehoteleira.api_hotel.model.entitites;

import com.redehoteleira.api_hotel.model.enums.RoomStatus;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "rooms")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id_room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hotel", nullable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private String number;
    private Integer capacity;

    @Column(name = "daily_rate", nullable = false)
    private Double dailyRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status;

    public Room() {}

    public Room(UUID id_room, Hotel hotel, String number, Integer capacity, Double dailyRate, RoomStatus status) {
        this.id_room = id_room;
        this.hotel = hotel;
        this.number = number;
        this.capacity = capacity;
        this.dailyRate = dailyRate;
        this.status = status;
    }

    public abstract Double calculateTotalRate(Integer days);

    public void changeStatus(RoomStatus newStatus) {
        this.status = newStatus;
    }

    public boolean isAvailable() {
        return this.status == RoomStatus.AVAILABLE;
    }

    public UUID getId_room() {
        return id_room;
    }

    public void setId_room(UUID id_room) {
        this.id_room = id_room;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(Double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }
}