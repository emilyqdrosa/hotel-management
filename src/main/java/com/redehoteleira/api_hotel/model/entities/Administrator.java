package com.redehoteleira.api_hotel.model.entities;

import com.redehoteleira.api_hotel.model.enums.SystemAccessLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "administrators")
@PrimaryKeyJoinColumn(name = "id_employee")
public class Administrator extends Employee {

    @Enumerated(EnumType.STRING)
    @Column(name = "system_access_level", nullable = false)
    private SystemAccessLevel systemAccessLevel;

    public Administrator() {
        super();
    }

    public Administrator(UUID id_employee, Hotel hotel, String name, String cpf, Double salary, LocalDate hireDate, WorkShift workShift, SystemAccessLevel systemAccessLevel) {
        super(id_employee, hotel, name, cpf, salary, hireDate, workShift);
        this.systemAccessLevel = systemAccessLevel;
    }

    @Override
    public Double calculateBonus() {
        Double percentageBonus = this.getSalary() * 0.20;
        return percentageBonus > 1000.0 ? percentageBonus : 1000.0;
    }

    public SystemAccessLevel getSystemAccessLevel() {
        return systemAccessLevel;
    }

    public void setSystemAccessLevel(SystemAccessLevel systemAccessLevel) {
        this.systemAccessLevel = systemAccessLevel;
    }
}