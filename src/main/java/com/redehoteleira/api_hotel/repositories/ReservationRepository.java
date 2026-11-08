package com.redehoteleira.api_hotel.repositories;

import com.redehoteleira.api_hotel.model.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.room.id = :roomId " +
            "AND r.status NOT IN (com.redehoteleira.api_hotel.model.enums.ReservationStatus.CANCELED, " +
            "com.redehoteleira.api_hotel.model.enums.ReservationStatus.COMPLETED) " +
            "AND r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate")
    boolean existsConflictingReservation(@Param("roomId") UUID roomId,
                                         @Param("checkInDate") LocalDate checkInDate,
                                         @Param("checkOutDate") LocalDate checkOutDate);
}