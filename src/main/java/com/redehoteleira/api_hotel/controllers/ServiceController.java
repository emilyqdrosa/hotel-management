package com.redehoteleira.api_hotel.controllers;

import com.redehoteleira.api_hotel.dtos.ServiceRequestDTO;
import com.redehoteleira.api_hotel.dtos.ServiceResponseDTO;
import com.redehoteleira.api_hotel.model.entities.Service;
import com.redehoteleira.api_hotel.services.ServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping
    public ResponseEntity<ServiceResponseDTO> create(@RequestBody @Valid ServiceRequestDTO data) {
        Service service = serviceService.createService(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service));
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> findAll() {
        List<ServiceResponseDTO> services = serviceService.getAllServices()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(serviceService.getServiceById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> update(@PathVariable UUID id,
                                                     @RequestBody @Valid ServiceRequestDTO data) {
        return ResponseEntity.ok(toResponse(serviceService.updateService(id, data)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    private ServiceResponseDTO toResponse(Service service) {
        return new ServiceResponseDTO(
                service.getId_service(),
                service.getType(),
                service.getName(),
                service.getPrice()
        );
    }
}