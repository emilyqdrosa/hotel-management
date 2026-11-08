package com.redehoteleira.api_hotel.model.entitites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "receptionists")
@PrimaryKeyJoinColumn(name = "id_employee")
public class Receptionist extends Employee {

    @Column(name = "spoken_languages")
    private String spokenLanguages;

    public Receptionist() {
        super();
    }

    public Receptionist(UUID id_employee, Hotel hotel, String name, String cpf, Double salary, LocalDate hireDate, WorkShift workShift, String spokenLanguages) {
        super(id_employee, hotel, name, cpf, salary, hireDate, workShift);
        this.spokenLanguages = spokenLanguages;
    }

    @Override
    public Double calculateBonus() {
        return this.getSalary() * 0.05;
    }

    public String getSpokenLanguages() {
        return spokenLanguages;
    }

    public void setSpokenLanguages(String spokenLanguages) {
        this.spokenLanguages = spokenLanguages;
    }
}