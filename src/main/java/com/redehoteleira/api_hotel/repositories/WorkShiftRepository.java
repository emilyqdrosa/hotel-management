package com.redehoteleira.api_hotel.repositories;

import com.redehoteleira.api_hotel.model.entities.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, UUID> {

}