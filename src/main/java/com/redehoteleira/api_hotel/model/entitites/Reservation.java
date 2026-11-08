package com.redehoteleira.api_hotel.model.entitites;

import com.redehoteleira.api_hotel.model.enums.ReservationStatus;
import com.redehoteleira.api_hotel.model.enums.RoomStatus;
import com.redehoteleira.api_hotel.model.enums.ServiceType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_reservation", updatable = false, nullable = false)
    private UUID id_reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_guest", nullable = false)
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_room", nullable = false)
    private Room room;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "total_value", nullable = false)
    private Double totalValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    // Coleção de Enums substituindo a Entidade Service
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "reservation_services",
            joinColumns = @JoinColumn(name = "id_reservation")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private List<ServiceType> consumedServices = new ArrayList<>();

    public Reservation() { }

    public Reservation(UUID id_reservation, Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate, Double totalValue, ReservationStatus status) {
        this.id_reservation = id_reservation;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalValue = totalValue;
        this.status = status;
    }

    public void calculateTotalValue() {
        if (this.checkInDate != null && this.checkOutDate != null && this.room != null) {
            int days = Math.toIntExact(ChronoUnit.DAYS.between(this.checkInDate, this.checkOutDate));
            if (days < 1) {
                throw new IllegalArgumentException("A quantidade de dias deve ser maior que zero.");
            }

            Double roomTotal = this.room.calculateTotalRate(days);

            Double servicesTotal = 0.0;
            if (this.consumedServices != null) {
                for (ServiceType service : consumedServices) {
                    servicesTotal += service.getDefaultPrice();
                }
            }

            this.totalValue = roomTotal + servicesTotal;
        }
    }

    public void addService(ServiceType service) {
        this.consumedServices.add(service);
        this.calculateTotalValue();
    }

    public void performCheckIn() {
        if (this.status == ReservationStatus.CONFIRMED) {
            this.room.changeStatus(RoomStatus.OCCUPIED);
        } else {
            throw new IllegalStateException("O check-in só pode ser realizado em reservas confirmadas.");
        }
    }

    public void performCheckOut() {
        this.status = ReservationStatus.COMPLETED;
        this.room.changeStatus(RoomStatus.AVAILABLE);
    }

    public void cancelReservation() {
        if (this.status == ReservationStatus.PENDING || this.status == ReservationStatus.CONFIRMED) {
            this.status = ReservationStatus.CANCELED;
            if (this.room.getStatus() == RoomStatus.RESERVED) {
                this.room.changeStatus(RoomStatus.AVAILABLE);
            }
        } else {
            throw new IllegalStateException("A reserva não pode ser cancelada no estado atual.");
        }
    }

    public UUID getId_reservation() { return id_reservation; }
    public void setId_reservation(UUID id_reservation) { this.id_reservation = id_reservation; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Double getTotalValue() { return totalValue; }
    public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public List<ServiceType> getConsumedServices() { return consumedServices; }
    public void setConsumedServices(List<ServiceType> consumedServices) { this.consumedServices = consumedServices; }
}