package com.redehoteleira.api_hotel.views;

import com.redehoteleira.api_hotel.dtos.ReservationRequestDTO;
import com.redehoteleira.api_hotel.model.entities.Guest;
import com.redehoteleira.api_hotel.model.entities.Hotel;
import com.redehoteleira.api_hotel.model.entities.LuxuryRoom;
import com.redehoteleira.api_hotel.model.entities.Reservation;
import com.redehoteleira.api_hotel.model.entities.Room;
import com.redehoteleira.api_hotel.model.enums.ReservationStatus;
import com.redehoteleira.api_hotel.model.enums.RoomStatus;
import com.redehoteleira.api_hotel.model.enums.ServiceType;
import com.redehoteleira.api_hotel.services.GuestService;
import com.redehoteleira.api_hotel.services.HotelService;
import com.redehoteleira.api_hotel.services.ReservationService;
import com.redehoteleira.api_hotel.services.RoomService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class ReservationView extends JPanel {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final String[] STATUS_FILTER = {"Todos", "Pendente", "Confirmada", "Cancelada", "Finalizada"};

    private record GuestItem(UUID id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record HotelItem(UUID id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record RoomItem(UUID id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private final ReservationService reservationService;
    private final GuestService guestService;
    private final HotelService hotelService;
    private final RoomService roomService;
    private final Validator validator;

    private final JComboBox<GuestItem> cbGuest = new JComboBox<>();
    private final JComboBox<HotelItem> cbHotel = new JComboBox<>();
    private final JComboBox<RoomItem> cbRoom = new JComboBox<>();
    private final JTextField txtCheckIn = new JTextField(12);
    private final JTextField txtCheckOut = new JTextField(12);
    private final JComboBox<ReservationStatus> cbStatus = new JComboBox<>(ReservationStatus.values());
    private final JPanel formPanel = new JPanel(new GridBagLayout());

    private final JTextField txtSearch = new JTextField(18);
    private final JComboBox<String> cbFilterStatus = new JComboBox<>(STATUS_FILTER);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Hóspede", "Hotel", "Quarto", "Entrada", "Saída", "Status",
                    "Status do quarto", "Serviços", "Valor total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    private final Map<UUID, Reservation> reservationsById = new HashMap<>();
    private List<Room> allRooms = new ArrayList<>();
    private UUID selectedId = null;
    private boolean loading = false;

    public ReservationView(ReservationService reservationService, GuestService guestService,
                           HotelService hotelService, RoomService roomService, Validator validator) {
        this.reservationService = reservationService;
        this.guestService = guestService;
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.validator = validator;

        buildUI();
        refresh();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        updateFormTitle();
        cbStatus.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ReservationStatus status) {
                    setText(statusLabel(status));
                }
                return this;
            }
        });
        cbStatus.setSelectedItem(ReservationStatus.PENDING);
        cbHotel.addActionListener(e -> {
            if (!loading) populateRooms();
        });

        addRow("Hóspede:", cbGuest, 0);
        addRow("Hotel:", cbHotel, 1);
        addRow("Quarto:", cbRoom, 2);
        addRow("Entrada (dd/mm/aaaa):", txtCheckIn, 3);
        addRow("Saída (dd/mm/aaaa):", txtCheckOut, 4);
        addRow("Status:", cbStatus, 5);

        JButton btnSave = new JButton("Salvar");
        JButton btnDelete = new JButton("Excluir");
        JButton btnClear = new JButton("Limpar");
        btnSave.addActionListener(e -> save());
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clearForm());

        JPanel crudButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        crudButtons.add(btnSave);
        crudButtons.add(btnDelete);
        crudButtons.add(btnClear);
        addRow("", crudButtons, 6);

        JButton btnCheckIn = new JButton("Check-in");
        JButton btnCheckOut = new JButton("Check-out");
        JButton btnCancel = new JButton("Cancelar reserva");
        JButton btnService = new JButton("Adicionar serviço");
        btnCheckIn.addActionListener(e -> checkIn());
        btnCheckOut.addActionListener(e -> checkOut());
        btnCancel.addActionListener(e -> cancelReservation());
        btnService.addActionListener(e -> addService());

        JPanel flowButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowButtons.add(btnCheckIn);
        flowButtons.add(btnCheckOut);
        flowButtons.add(btnCancel);
        flowButtons.add(btnService);
        addRow("", flowButtons, 7);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Buscar (hóspede, hotel ou quarto):"));
        filterPanel.add(txtSearch);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cbFilterStatus);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        cbFilterStatus.addActionListener(e -> applyFilter());

        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.removeColumn(table.getColumnModel().getColumn(0)); // esconde ID
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });

        JPanel north = new JPanel(new BorderLayout());
        north.add(formPanel, BorderLayout.NORTH);
        north.add(filterPanel, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void addRow(String label, Component component, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = row;

        gbc.gridx = 0;
        formPanel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        formPanel.add(component, gbc);
    }

    public void refresh() {
        loading = true;
        try {
            loadGuests();
            loadHotels();
            allRooms = roomService.getAllRooms();
            populateRooms();
        } catch (Exception ex) {
            showError("Erro ao carregar dados: " + ex.getMessage());
        } finally {
            loading = false;
        }
        loadReservations();
    }

    private void loadGuests() {
        GuestItem previous = (GuestItem) cbGuest.getSelectedItem();

        DefaultComboBoxModel<GuestItem> model = new DefaultComboBoxModel<>();
        for (Guest g : guestService.getAllGuests()) {
            model.addElement(new GuestItem(g.getId_guest(), g.getName() + " — " + g.getCpf()));
        }
        cbGuest.setModel(model);

        if (previous != null) selectById(cbGuest, previous.id(), GuestItem::id);
    }

    private void loadHotels() {
        HotelItem previous = (HotelItem) cbHotel.getSelectedItem();

        DefaultComboBoxModel<HotelItem> model = new DefaultComboBoxModel<>();
        for (Hotel h : hotelService.getAllHotels()) {
            model.addElement(new HotelItem(h.getId(), h.getName()));
        }
        cbHotel.setModel(model);

        if (previous != null) selectById(cbHotel, previous.id(), HotelItem::id);
    }

    private void populateRooms() {
        RoomItem previous = (RoomItem) cbRoom.getSelectedItem();
        HotelItem hotel = (HotelItem) cbHotel.getSelectedItem();

        DefaultComboBoxModel<RoomItem> model = new DefaultComboBoxModel<>();
        if (hotel != null) {
            allRooms.stream()
                    .filter(r -> r.getHotel().getId().equals(hotel.id()))
                    .sorted(Comparator.comparing(Room::getNumber))
                    .forEach(r -> model.addElement(new RoomItem(r.getId_room(), roomLabel(r))));
        }
        cbRoom.setModel(model);

        if (previous != null) selectById(cbRoom, previous.id(), RoomItem::id);
    }

    private void loadReservations() {
        try {
            List<Reservation> reservations = reservationService.getAllReservations();

            reservationsById.clear();
            tableModel.setRowCount(0);
            for (Reservation r : reservations) {
                reservationsById.put(r.getId_reservation(), r);

                String services = r.getConsumedServices().isEmpty()
                        ? "—"
                        : r.getConsumedServices().stream()
                        .map(ReservationView::serviceLabel)
                        .collect(Collectors.joining(", "));

                tableModel.addRow(new Object[]{
                        r.getId_reservation(),
                        r.getGuest().getName(),
                        r.getRoom().getHotel().getName(),
                        r.getRoom().getNumber(),
                        r.getCheckInDate().format(DATE_FORMAT),
                        r.getCheckOutDate().format(DATE_FORMAT),
                        statusLabel(r.getStatus()),
                        roomStatusLabel(r.getRoom().getStatus()),
                        services,
                        money(r.getTotalValue())
                });
            }
        } catch (Exception ex) {
            showError("Erro ao carregar reservas: " + ex.getMessage());
        }
    }

    private void save() {
        GuestItem guest = (GuestItem) cbGuest.getSelectedItem();
        RoomItem room = (RoomItem) cbRoom.getSelectedItem();
        ReservationStatus status = (ReservationStatus) cbStatus.getSelectedItem();

        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = parseDate(txtCheckIn.getText());
            checkOut = parseDate(txtCheckOut.getText());
        } catch (DateTimeParseException ex) {
            warn("Data inválida. Use o formato dd/mm/aaaa.");
            return;
        }

        Reservation existing = selectedId == null ? null : reservationsById.get(selectedId);
        if (existing != null && isClosed(existing.getStatus())) {
            warn("Reservas canceladas ou finalizadas não podem ser editadas.");
            return;
        }
        if (isClosed(status)) {
            warn("Para cancelar ou finalizar uma reserva, use os botões \"Cancelar reserva\" e \"Check-out\".");
            return;
        }

        List<ServiceType> services = existing == null
                ? new ArrayList<>()
                : new ArrayList<>(existing.getConsumedServices());

        ReservationRequestDTO dto = new ReservationRequestDTO(
                guest == null ? null : guest.id(),
                room == null ? null : room.id(),
                checkIn,
                checkOut,
                status,
                services
        );

        String validationError = validate(dto);
        if (validationError != null) {
            warn(validationError);
            return;
        }

        try {
            if (selectedId == null) {
                reservationService.createReservation(dto);
                showInfo("Reserva cadastrada com sucesso!");
            } else {
                reservationService.updateReservation(selectedId, dto);
                showInfo("Reserva atualizada com sucesso!");
            }
            clearForm();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro inesperado ao salvar: " + ex.getMessage());
        }
    }

    private void delete() {
        if (!hasSelection()) return;

        int option = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir esta reserva?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            reservationService.deleteReservation(selectedId);
            showInfo("Reserva excluída com sucesso!");
            clearForm();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Não foi possível excluir a reserva: " + ex.getMessage());
        }
    }

    private void checkIn() {
        if (!hasSelection()) return;

        try {
            reservationService.performCheckIn(selectedId);
            showInfo("Check-in realizado. O quarto agora está ocupado.");
            clearForm();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro ao realizar check-in: " + ex.getMessage());
        }
    }

    private void checkOut() {
        if (!hasSelection()) return;

        Reservation reservation = reservationsById.get(selectedId);
        if (reservation == null
                || reservation.getStatus() != ReservationStatus.CONFIRMED
                || reservation.getRoom().getStatus() != RoomStatus.OCCUPIED) {
            warn("O check-out só pode ser feito em reservas confirmadas que já tiveram check-in.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                String.format(PT_BR, "Finalizar a hospedagem?\nValor total: %s", money(reservation.getTotalValue())),
                "Confirmar check-out", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            reservationService.performCheckOut(selectedId);
            showInfo("Check-out realizado. O quarto está disponível novamente.");
            clearForm();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro ao realizar check-out: " + ex.getMessage());
        }
    }

    private void cancelReservation() {
        if (!hasSelection()) return;

        int option = JOptionPane.showConfirmDialog(this,
                "Deseja realmente cancelar esta reserva?", "Confirmar cancelamento", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            reservationService.cancelReservation(selectedId);
            showInfo("Reserva cancelada.");
            clearForm();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro ao cancelar a reserva: " + ex.getMessage());
        }
    }

    private void addService() {
        if (!hasSelection()) return;

        Reservation reservation = reservationsById.get(selectedId);
        if (reservation != null && isClosed(reservation.getStatus())) {
            warn("Não é possível adicionar serviços a uma reserva cancelada ou finalizada.");
            return;
        }

        JComboBox<ServiceType> cbService = new JComboBox<>(ServiceType.values());
        cbService.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ServiceType type) {
                    setText(serviceLabel(type) + " (" + money(type.getDefaultPrice()) + ")");
                }
                return this;
            }
        });

        int option = JOptionPane.showConfirmDialog(this, cbService, "Adicionar serviço",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            reservationService.addServiceToReservation(selectedId, (ServiceType) cbService.getSelectedItem());
            showInfo("Serviço adicionado. O valor total foi atualizado.");
            clearForm();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro ao adicionar serviço: " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedId = null;
        txtCheckIn.setText("");
        txtCheckOut.setText("");
        cbStatus.setSelectedItem(ReservationStatus.PENDING);
        table.clearSelection();
        updateFormTitle();
    }

    private void fillFormFromSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        UUID id = (UUID) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
        Reservation r = reservationsById.get(id);
        if (r == null) {
            return;
        }

        selectedId = id;
        selectById(cbGuest, r.getGuest().getId_guest(), GuestItem::id);
        selectById(cbHotel, r.getRoom().getHotel().getId(), HotelItem::id); // recarrega os quartos do hotel
        selectById(cbRoom, r.getRoom().getId_room(), RoomItem::id);
        txtCheckIn.setText(r.getCheckInDate().format(DATE_FORMAT));
        txtCheckOut.setText(r.getCheckOutDate().format(DATE_FORMAT));
        cbStatus.setSelectedItem(r.getStatus());
        updateFormTitle();
    }

    private void applyFilter() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();

        String text = txtSearch.getText().trim();
        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2, 3));
        }
        if (cbFilterStatus.getSelectedIndex() > 0) {
            String status = (String) cbFilterStatus.getSelectedItem();
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(status) + "$", 6));
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private boolean hasSelection() {
        if (selectedId == null) {
            warn("Selecione uma reserva na tabela.");
            return false;
        }
        return true;
    }

    private <T> void selectById(JComboBox<T> combo, UUID id, Function<T, UUID> idOf) {
        ComboBoxModel<T> model = combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (Objects.equals(idOf.apply(model.getElementAt(i)), id)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private static boolean isClosed(ReservationStatus status) {
        return status == ReservationStatus.CANCELED || status == ReservationStatus.COMPLETED;
    }

    private String validate(ReservationRequestDTO dto) {
        String errors = validator.validate(dto).stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining("\n"));
        return errors.isEmpty() ? null : errors;
    }

    private LocalDate parseDate(String text) {
        String value = text.trim();
        return value.isEmpty() ? null : LocalDate.parse(value, DATE_FORMAT);
    }

    private static String money(Double value) {
        return value == null ? "" : String.format(PT_BR, "R$ %.2f", value);
    }

    private static String roomLabel(Room room) {
        String type = (room instanceof LuxuryRoom) ? "Luxo" : "Standard";
        return "Nº " + room.getNumber() + " — " + type + " — " + money(room.getDailyRate())
                + " — " + roomStatusLabel(room.getStatus());
    }

    private static String statusLabel(ReservationStatus status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING -> "Pendente";
            case CONFIRMED -> "Confirmada";
            case CANCELED -> "Cancelada";
            case COMPLETED -> "Finalizada";
        };
    }

    private static String roomStatusLabel(RoomStatus status) {
        if (status == null) return "";
        return switch (status) {
            case AVAILABLE -> "Disponível";
            case RESERVED -> "Reservado";
            case OCCUPIED -> "Ocupado";
            case MAINTENANCE -> "Manutenção";
        };
    }

    private static String serviceLabel(ServiceType type) {
        return switch (type) {
            case ROOM_SERVICE -> "Serviço de quarto";
            case LAUNDRY -> "Lavanderia";
            case BREAKFAST -> "Café da manhã";
            case PARKING -> "Estacionamento";
        };
    }

    private void updateFormTitle() {
        String title = (selectedId == null) ? "Nova reserva" : "Editando reserva";
        formPanel.setBorder(BorderFactory.createTitledBorder(title));
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}