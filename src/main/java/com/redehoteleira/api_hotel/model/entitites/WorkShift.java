package com.redehoteleira.api_hotel.model.entitites;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "work_shifts")
public class WorkShift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_work_shift", updatable = false, nullable = false)
    private UUID id_work_shift;

    @Column(nullable = false)
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "working_days", nullable = false)
    private String workingDays;

    public WorkShift() {
    }

    public WorkShift(UUID id_work_shift, String description, LocalTime startTime, LocalTime endTime, String workingDays) {
        this.id_work_shift = id_work_shift;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workingDays = workingDays;
    }

    public UUID getId_work_shift() { return id_work_shift; }

    public void setId_work_shift(UUID id_work_shift) { this.id_work_shift = id_work_shift; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public LocalTime getStartTime() { return startTime; }

    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }

    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getWorkingDays() { return workingDays; }

    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }
}