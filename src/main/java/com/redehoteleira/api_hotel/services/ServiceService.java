package com.redehoteleira.api_hotel.services;

import com.redehoteleira.api_hotel.dtos.ServiceRequestDTO;
import com.redehoteleira.api_hotel.model.entitites.Service;
import com.redehoteleira.api_hotel.repositories.ServiceRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Transactional
    public Service createService(ServiceRequestDTO data) {
        if (serviceRepository.existsByType(data.type())) {
            throw new IllegalArgumentException("Já existe um serviço cadastrado com este tipo.");
        }

        Service service = new Service(null, data.type(), data.name());

        return serviceRepository.save(service);
    }

    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public Service getServiceById(UUID id) {
        return serviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));
    }

    @Transactional
    public Service updateService(UUID id, ServiceRequestDTO data) {
        Service service = getServiceById(id);

        if (service.getType() != data.type() && serviceRepository.existsByType(data.type())) {
            throw new IllegalArgumentException("Já existe outro serviço cadastrado com este tipo.");
        }

        service.setType(data.type());
        service.setName(data.name() != null ? data.name() : data.type().name());

        if (data.price() != null) {
            service.setPrice(data.price());
        }

        return serviceRepository.save(service);
    }

    @Transactional
    public void deleteService(UUID id) {
        Service service = getServiceById(id);
        serviceRepository.delete(service);
    }
}