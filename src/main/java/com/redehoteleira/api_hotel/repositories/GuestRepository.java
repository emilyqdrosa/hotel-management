package com.redehoteleira.api_hotel.repositories;

import com.redehoteleira.api_hotel.model.entities.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GuestRepository extends JpaRepository<Guest, UUID> {

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);
}