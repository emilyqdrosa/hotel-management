package com.redehoteleira.api_hotel.services;

import com.redehoteleira.api_hotel.dtos.WorkShiftRequestDTO;
import com.redehoteleira.api_hotel.model.entities.WorkShift;
import com.redehoteleira.api_hotel.repositories.WorkShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WorkShiftService {

    private final WorkShiftRepository workShiftRepository;

    public WorkShiftService(WorkShiftRepository workShiftRepository) {
        this.workShiftRepository = workShiftRepository;
    }

    @Transactional
    public WorkShift createWorkShift(WorkShiftRequestDTO data) {
        WorkShift workShift = new WorkShift();
        workShift.setDescription(data.description());
        workShift.setStartTime(data.startTime());
        workShift.setEndTime(data.endTime());
        workShift.setWorkingDays(data.workingDays());

        return workShiftRepository.save(workShift);
    }

    public List<WorkShift> getAllWorkShifts() {
        return workShiftRepository.findAll();
    }

    public WorkShift getWorkShiftById(UUID id) {
        return workShiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turno de trabalho não encontrado."));
    }

    @Transactional
    public WorkShift updateWorkShift(UUID id, WorkShiftRequestDTO data) {
        WorkShift workShift = getWorkShiftById(id);

        workShift.setDescription(data.description());
        workShift.setStartTime(data.startTime());
        workShift.setEndTime(data.endTime());
        workShift.setWorkingDays(data.workingDays());

        return workShiftRepository.save(workShift);
    }

    @Transactional
    public void deleteWorkShift(UUID id) {
        WorkShift workShift = getWorkShiftById(id);
        workShiftRepository.delete(workShift);
    }
}