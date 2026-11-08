package com.redehoteleira.api_hotel.repositories;

import com.redehoteleira.api_hotel.model.entitites.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
}