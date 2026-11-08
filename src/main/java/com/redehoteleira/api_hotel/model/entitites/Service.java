package com.redehoteleira.api_hotel.model.entitites;

import com.redehoteleira.api_hotel.model.enums.ServiceType;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_service", updatable = false, nullable = false)
    private UUID id_service;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    public Service() {
    }

    public Service(UUID id_service, ServiceType type, String customName) {
        this.id_service = id_service;
        this.type = type;
        this.name = customName != null ? customName : type.name();
        this.price = type.getDefaultPrice();
    }

    // Getters e Setters
    public UUID getId_service() {
        return id_service;
    }

    public void setId_service(UUID id_service) {
        this.id_service = id_service;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}