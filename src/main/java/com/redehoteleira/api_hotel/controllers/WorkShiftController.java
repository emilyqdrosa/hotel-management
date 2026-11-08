package com.redehoteleira.api_hotel.controllers;

import com.redehoteleira.api_hotel.dtos.WorkShiftRequestDTO;
import com.redehoteleira.api_hotel.dtos.WorkShiftResponseDTO;
import com.redehoteleira.api_hotel.model.entities.WorkShift;
import com.redehoteleira.api_hotel.services.WorkShiftService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/work-shifts")
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    public WorkShiftController(WorkShiftService workShiftService) {
        this.workShiftService = workShiftService;
    }

    @PostMapping
    public ResponseEntity<WorkShiftResponseDTO> create(@RequestBody @Valid WorkShiftRequestDTO data) {
        WorkShift workShift = workShiftService.createWorkShift(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(workShift));
    }

    @GetMapping
    public ResponseEntity<List<WorkShiftResponseDTO>> findAll() {
        List<WorkShiftResponseDTO> workShifts = workShiftService.getAllWorkShifts()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(workShifts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkShiftResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(workShiftService.getWorkShiftById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkShiftResponseDTO> update(@PathVariable UUID id,
                                                       @RequestBody @Valid WorkShiftRequestDTO data) {
        return ResponseEntity.ok(toResponse(workShiftService.updateWorkShift(id, data)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        workShiftService.deleteWorkShift(id);
        return ResponseEntity.noContent().build();
    }

    private WorkShiftResponseDTO toResponse(WorkShift workShift) {
        return new WorkShiftResponseDTO(
                workShift.getId_work_shift(),
                workShift.getDescription(),
                workShift.getStartTime(),
                workShift.getEndTime(),
                workShift.getWorkingDays()
        );
    }
}