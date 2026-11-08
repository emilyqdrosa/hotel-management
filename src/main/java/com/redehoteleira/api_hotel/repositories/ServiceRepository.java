package com.redehoteleira.api_hotel.repositories;

import com.redehoteleira.api_hotel.model.entitites.Service;
import com.redehoteleira.api_hotel.model.enums.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    boolean existsByType(ServiceType type);

    boolean existsByName(String name);
}