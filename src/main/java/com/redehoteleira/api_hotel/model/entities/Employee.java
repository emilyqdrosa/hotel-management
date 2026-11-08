package com.redehoteleira.api_hotel.model.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Inheritance(strategy = InheritanceType.JOINED)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_employee", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hotel", nullable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false)
    private Double salary;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_work_shift")
    private WorkShift workShift;

    @Column(name = "is_on_vacation", nullable = false)
    private Boolean isOnVacation = false;

    public Employee() {
    }

    public Employee(UUID id_employee, Hotel hotel, String name, String cpf, Double salary, LocalDate hireDate, WorkShift workShift) {
        this.id = id_employee;
        this.hotel = hotel;
        this.name = name;
        this.cpf = cpf;
        this.salary = salary;
        this.hireDate = hireDate;
        this.workShift = workShift;
        this.isOnVacation = false;
    }

    public Double calculateBonus() {
        return 0.0;
    }

    public void startVacation() {
        this.isOnVacation = true;
    }

    public void endVacation() {
        this.isOnVacation = false;
    }

    public UUID getId_employee() {
        return id;
    }

    public void setId_employee(UUID id_employee) {
        this.id = id_employee;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public WorkShift getWorkShift() {
        return workShift;
    }

    public void setWorkShift(WorkShift workShift) {
        this.workShift = workShift;
    }

    public Boolean getIsOnVacation() {
        return isOnVacation;
    }

    public void setIsOnVacation(Boolean isOnVacation) {
        this.isOnVacation = isOnVacation;
    }
}