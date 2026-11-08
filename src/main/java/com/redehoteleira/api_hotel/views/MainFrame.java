package com.redehoteleira.api_hotel.views;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Component
public class MainFrame extends JFrame {

    public MainFrame(HotelView hotelView,
                     RoomView roomView,
                     EmployeeView employeeView,
                     WorkShiftView workShiftView,
                     GuestView guestView,
                     ReservationView reservationView,
                     ServiceView serviceView) {

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
        tabs.addTab("Serviços", serviceView);

        Map<JComponent, Runnable> refreshers = new HashMap<>();
        refreshers.put(hotelView, hotelView::refresh);
        refreshers.put(roomView, roomView::refresh);
        refreshers.put(employeeView, employeeView::refresh);
        refreshers.put(workShiftView, workShiftView::refresh);
        refreshers.put(guestView, guestView::refresh);
        refreshers.put(reservationView, reservationView::refresh);
        refreshers.put(serviceView, serviceView::refresh);

        tabs.addChangeListener(e -> {
            Runnable refresher = refreshers.get(tabs.getSelectedComponent());
            if (refresher != null) {
                refresher.run();
            }
        });

        add(tabs);
    }
}