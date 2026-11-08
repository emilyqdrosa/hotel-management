package com.redehoteleira.api_hotel.views;

import com.redehoteleira.api_hotel.dtos.LuxuryRoomRequestDTO;
import com.redehoteleira.api_hotel.dtos.RoomRequestDTO;
import com.redehoteleira.api_hotel.dtos.StandardRoomRequestDTO;
import com.redehoteleira.api_hotel.model.entities.Hotel;
import com.redehoteleira.api_hotel.model.entities.LuxuryRoom;
import com.redehoteleira.api_hotel.model.entities.Room;
import com.redehoteleira.api_hotel.model.entities.StandardRoom;
import com.redehoteleira.api_hotel.model.enums.RoomStatus;
import com.redehoteleira.api_hotel.services.HotelService;
import com.redehoteleira.api_hotel.services.RoomService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.springframework.stereotype.Component
public class RoomView extends JPanel {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private record HotelItem(UUID id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private final RoomService roomService;
    private final HotelService hotelService;
    private final Validator validator;

    private final JComboBox<HotelItem> cbHotel = new JComboBox<>();
    private final JComboBox<String> cbType = new JComboBox<>(new String[]{"Standard", "Luxo"});
    private final JTextField txtNumber = new JTextField(15);
    private final JTextField txtCapacity = new JTextField(15);
    private final JTextField txtDailyRate = new JTextField(15);
    private final JComboBox<RoomStatus> cbStatus = new JComboBox<>(RoomStatus.values());
    private final JCheckBox chkAirConditioning = new JCheckBox("Possui ar-condicionado");
    private final JCheckBox chkJacuzzi = new JCheckBox("Possui jacuzzi");
    private final JPanel formPanel = new JPanel(new GridBagLayout());

    private final JComboBox<HotelItem> cbFilterHotel = new JComboBox<>();
    private final JCheckBox chkOnlyAvailable = new JCheckBox("Somente disponíveis");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "ID Hotel", "Hotel", "Número", "Tipo", "Capacidade", "Diária", "Status", "Extra"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final Map<UUID, Room> roomsById = new HashMap<>();
    private UUID selectedId = null;
    private boolean loading = false;

    public RoomView(RoomService roomService, HotelService hotelService, Validator validator) {
        this.roomService = roomService;
        this.hotelService = hotelService;
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
                if (value instanceof RoomStatus status) {
                    setText(statusLabel(status));
                }
                return this;
            }
        });
        cbType.addActionListener(e -> updateTypeFields());

        JPanel extraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        extraPanel.add(chkAirConditioning);
        extraPanel.add(chkJacuzzi);

        addRow("Hotel:", cbHotel, 0);
        addRow("Tipo:", cbType, 1);
        addRow("Número:", txtNumber, 2);
        addRow("Capacidade:", txtCapacity, 3);
        addRow("Diária (R$):", txtDailyRate, 4);
        addRow("Status:", cbStatus, 5);
        addRow("Extra:", extraPanel, 6);

        JButton btnSave = new JButton("Salvar");
        JButton btnDelete = new JButton("Excluir");
        JButton btnClear = new JButton("Limpar");
        btnSave.addActionListener(e -> save());
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(btnSave);
        buttons.add(btnDelete);
        buttons.add(btnClear);
        addRow("", buttons, 7);

        updateTypeFields();

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filtrar por hotel:"));
        filterPanel.add(cbFilterHotel);
        filterPanel.add(chkOnlyAvailable);
        cbFilterHotel.addActionListener(e -> {
            if (!loading) loadRooms();
        });
        chkOnlyAvailable.addActionListener(e -> {
            if (!loading) loadRooms();
        });

        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.removeColumn(table.getColumnModel().getColumn(0)); // esconde ID do quarto
        table.removeColumn(table.getColumnModel().getColumn(0)); // esconde ID do hotel
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
            loadHotels();
        } catch (Exception ex) {
            showError("Erro ao carregar hotéis: " + ex.getMessage());
        } finally {
            loading = false;
        }
        loadRooms();
    }

    private void loadHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();

        HotelItem previousForm = (HotelItem) cbHotel.getSelectedItem();
        HotelItem previousFilter = (HotelItem) cbFilterHotel.getSelectedItem();

        DefaultComboBoxModel<HotelItem> formModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<HotelItem> filterModel = new DefaultComboBoxModel<>();
        filterModel.addElement(new HotelItem(null, "Todos os hotéis"));

        for (Hotel hotel : hotels) {
            HotelItem item = new HotelItem(hotel.getId(), hotel.getName());
            formModel.addElement(item);
            filterModel.addElement(item);
        }

        cbHotel.setModel(formModel);
        cbFilterHotel.setModel(filterModel);

        if (previousForm != null) selectHotel(cbHotel, previousForm.id());
        if (previousFilter != null) selectHotel(cbFilterHotel, previousFilter.id());
    }

    private void loadRooms() {
        try {
            HotelItem filter = (HotelItem) cbFilterHotel.getSelectedItem();
            boolean onlyAvailable = chkOnlyAvailable.isSelected();

            List<Room> rooms = roomService.getAllRooms().stream()
                    .filter(r -> filter == null || filter.id() == null || r.getHotel().getId().equals(filter.id()))
                    .filter(r -> !onlyAvailable || r.isAvailable())
                    .toList();

            roomsById.clear();
            tableModel.setRowCount(0);
            for (Room room : rooms) {
                roomsById.put(room.getId_room(), room);
                tableModel.addRow(new Object[]{
                        room.getId_room(),
                        room.getHotel().getId(),
                        room.getHotel().getName(),
                        room.getNumber(),
                        typeLabel(room),
                        room.getCapacity(),
                        String.format(PT_BR, "R$ %.2f", room.getDailyRate()),
                        statusLabel(room.getStatus()),
                        extraLabel(room)
                });
            }
        } catch (Exception ex) {
            showError("Erro ao carregar quartos: " + ex.getMessage());
        }
    }

    private void save() {
        HotelItem hotel = (HotelItem) cbHotel.getSelectedItem();
        boolean luxury = cbType.getSelectedIndex() == 1;

        Integer capacity;
        Double dailyRate;
        try {
            capacity = parseInteger(txtCapacity.getText());
            dailyRate = parseDouble(txtDailyRate.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacidade e diária devem ser números válidos.",
                    "Dados inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RoomRequestDTO base = new RoomRequestDTO(
                txtNumber.getText().trim(),
                capacity,
                dailyRate,
                (RoomStatus) cbStatus.getSelectedItem(),
                hotel == null ? null : hotel.id()
        );
        StandardRoomRequestDTO standard = new StandardRoomRequestDTO(chkAirConditioning.isSelected());
        LuxuryRoomRequestDTO luxuryData = new LuxuryRoomRequestDTO(chkJacuzzi.isSelected());

        String validationError = luxury ? validate(base, luxuryData) : validate(base, standard);
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError, "Dados inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (selectedId == null) {
                if (luxury) {
                    roomService.createLuxuryRoom(base, luxuryData);
                } else {
                    roomService.createStandardRoom(base, standard);
                }
                showInfo("Quarto cadastrado com sucesso!");
            } else {
                if (luxury) {
                    roomService.updateLuxuryRoom(selectedId, base, luxuryData);
                } else {
                    roomService.updateStandardRoom(selectedId, base, standard);
                }
                showInfo("Quarto atualizado com sucesso!");
            }
            clearForm();
            loadRooms();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro inesperado ao salvar: " + ex.getMessage());
        }
    }

    private void delete() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Selecione um quarto na tabela para excluir.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir este quarto?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            roomService.deleteRoom(selectedId);
            showInfo("Quarto excluído com sucesso!");
            clearForm();
            loadRooms();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Não foi possível excluir o quarto. Ele pode possuir reservas vinculadas.");
        }
    }

    private void clearForm() {
        selectedId = null;
        txtNumber.setText("");
        txtCapacity.setText("");
        txtDailyRate.setText("");
        cbStatus.setSelectedItem(RoomStatus.AVAILABLE);
        chkAirConditioning.setSelected(false);
        chkJacuzzi.setSelected(false);
        cbType.setSelectedIndex(0);
        cbType.setEnabled(true);
        table.clearSelection();
        updateTypeFields();
        updateFormTitle();
    }

    private void fillFormFromSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        UUID id = (UUID) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
        Room room = roomsById.get(id);
        if (room == null) {
            return;
        }

        selectedId = id;
        selectHotel(cbHotel, room.getHotel().getId());
        txtNumber.setText(room.getNumber());
        txtCapacity.setText(room.getCapacity() == null ? "" : String.valueOf(room.getCapacity()));
        txtDailyRate.setText(room.getDailyRate() == null ? "" : String.valueOf(room.getDailyRate()));
        cbStatus.setSelectedItem(room.getStatus());

        if (room instanceof StandardRoom standardRoom) {
            cbType.setSelectedIndex(0);
            chkAirConditioning.setSelected(Boolean.TRUE.equals(standardRoom.getHasAirConditioning()));
        } else if (room instanceof LuxuryRoom luxuryRoom) {
            cbType.setSelectedIndex(1);
            chkJacuzzi.setSelected(Boolean.TRUE.equals(luxuryRoom.getHasJacuzzi()));
        }

        cbType.setEnabled(false);
        updateTypeFields();
        updateFormTitle();
    }

    private void selectHotel(JComboBox<HotelItem> combo, UUID id) {
        ComboBoxModel<HotelItem> model = combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (Objects.equals(model.getElementAt(i).id(), id)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void updateTypeFields() {
        boolean luxury = cbType.getSelectedIndex() == 1;
        chkAirConditioning.setVisible(!luxury);
        chkJacuzzi.setVisible(luxury);
        formPanel.revalidate();
        formPanel.repaint();
    }

    private void updateFormTitle() {
        String title = (selectedId == null) ? "Novo quarto" : "Editando quarto";
        formPanel.setBorder(BorderFactory.createTitledBorder(title));
    }

    private String validate(RoomRequestDTO base, Object specific) {
        Stream<String> baseErrors = validator.validate(base).stream().map(ConstraintViolation::getMessage);
        Stream<String> specificErrors = validator.validate(specific).stream().map(ConstraintViolation::getMessage);
        String errors = Stream.concat(baseErrors, specificErrors).sorted().collect(Collectors.joining("\n"));
        return errors.isEmpty() ? null : errors;
    }

    private Integer parseInteger(String text) {
        String value = text.trim();
        return value.isEmpty() ? null : Integer.valueOf(value);
    }

    private Double parseDouble(String text) {
        String value = text.trim().replace(',', '.');
        return value.isEmpty() ? null : Double.valueOf(value);
    }

    private static String statusLabel(RoomStatus status) {
        if (status == null) return "";
        return switch (status) {
            case AVAILABLE -> "Disponível";
            case RESERVED -> "Reservado";
            case OCCUPIED -> "Ocupado";
            case MAINTENANCE -> "Manutenção";
        };
    }

    private static String typeLabel(Room room) {
        return (room instanceof LuxuryRoom) ? "Luxo" : "Standard";
    }

    private static String extraLabel(Room room) {
        if (room instanceof StandardRoom standardRoom) {
            return "Ar-condicionado: " + (Boolean.TRUE.equals(standardRoom.getHasAirConditioning()) ? "Sim" : "Não");
        }
        if (room instanceof LuxuryRoom luxuryRoom) {
            return "Jacuzzi: " + (Boolean.TRUE.equals(luxuryRoom.getHasJacuzzi()) ? "Sim" : "Não");
        }
        return "";
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}