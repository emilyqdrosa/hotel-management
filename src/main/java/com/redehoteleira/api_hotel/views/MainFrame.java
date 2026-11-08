package com.redehoteleira.api_hotel.views;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class MainFrame extends JFrame {

    public MainFrame(HotelView hotelView,
                     RoomView roomView,
                     EmployeeView employeeView,
                     WorkShiftView workShiftView,
                     GuestView guestView,
                     ReservationView reservationView) {

        setTitle("Rede Hoteleira");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Hotéis", hotelView);
        tabs.addTab("Quartos", roomView);
        tabs.addTab("Funcionários", employeeView);
        tabs.addTab("Turnos", workShiftView);
        tabs.addTab("Hóspedes", guestView);
        tabs.addTab("Reservas", reservationView);

        // Ao trocar de aba, recarrega os dados (combos e tabelas ficam sempre atualizados)
        Map<JComponent, Runnable> refreshers = new HashMap<>();
        refreshers.put(hotelView, hotelView::refresh);
        refreshers.put(roomView, roomView::refresh);
        refreshers.put(employeeView, employeeView::refresh);
        refreshers.put(workShiftView, workShiftView::refresh);
        refreshers.put(guestView, guestView::refresh);
        refreshers.put(reservationView, reservationView::refresh);

        tabs.addChangeListener(e -> {
            Runnable refresher = refreshers.get(tabs.getSelectedComponent());
            if (refresher != null) {
                refresher.run();
            }
        });

        add(tabs);
    }
}