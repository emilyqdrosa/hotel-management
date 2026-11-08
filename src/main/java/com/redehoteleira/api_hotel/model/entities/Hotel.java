package com.redehoteleira.api_hotel.model.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_hotel", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(nullable = false)
    private String address;
    private String phone;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees = new ArrayList<>();

    public Hotel() {}

    public Hotel(UUID id, String name, String address, String phone, List<Room> rooms, List<Employee> employees) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.rooms = rooms != null ? rooms : new ArrayList<>();
        this.employees = employees != null ? employees : new ArrayList<>();
    }

    public void getHotelInfo() {
        System.out.println(String.format("Hotel: %s | Endereço: %s | Telefone: %s",
                this.name, this.address, this.phone));
    }

    public void addRoom(Room room) {
        this.rooms.add(room);
        room.setHotel(this);
    }

    public void removeRoom(Room room) {
        this.rooms.remove(room);
        room.setHotel(null);
    }

    public void addEmployee(Employee employee) {
        this.employees.add(employee);
        employee.setHotel(this);
    }

    public void removeEmployee(Employee employee) {
        this.employees.remove(employee);
        employee.setHotel(null);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
}