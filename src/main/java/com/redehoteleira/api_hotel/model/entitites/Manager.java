package com.redehoteleira.api_hotel.model.entitites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "managers")
@PrimaryKeyJoinColumn(name = "id_employee")
public class Manager extends Employee {

    @Column(name = "department")
    private String department;

    public Manager() {
        super();
    }

    public Manager(UUID id_employee, Hotel hotel, String name, String cpf, Double salary, LocalDate hireDate, WorkShift workShift, String department) {
        super(id_employee, hotel, name, cpf, salary, hireDate, workShift);
        this.department = department;
    }

    @Override
    public Double calculateBonus() {
        return this.getSalary() * 0.10;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}