package com.redehoteleira.api_hotel.repositories;

import com.redehoteleira.api_hotel.model.entities.Hotel;
import com.redehoteleira.api_hotel.model.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    boolean existsByNumberAndHotel(String number, Hotel hotel);
}