package com.redehoteleira.api_hotel.views;

import com.redehoteleira.api_hotel.dtos.GuestRequestDTO;
import com.redehoteleira.api_hotel.model.entities.Guest;
import com.redehoteleira.api_hotel.model.entities.Reservation;
import com.redehoteleira.api_hotel.model.enums.ReservationStatus;
import com.redehoteleira.api_hotel.services.GuestService;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class GuestView extends JPanel {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final GuestService guestService;
    private final Validator validator;

    private final JTextField txtName = new JTextField(25);
    private final JTextField txtCpf = new JTextField(25);
    private final JTextField txtEmail = new JTextField(25);
    private final JTextField txtPhone = new JTextField(25);
    private final JTextField txtBirthDate = new JTextField(25);
    private final JTextField txtSearch = new JTextField(20);
    private final JPanel formPanel = new JPanel(new GridBagLayout());

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Nome", "CPF", "E-mail", "Telefone", "Nascimento"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    private UUID selectedId = null;

    public GuestView(GuestService guestService, Validator validator) {
        this.guestService = guestService;
        this.validator = validator;

        buildUI();
        refresh();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        updateFormTitle();
        addRow("Nome:", txtName, 0);
        addRow("CPF:", txtCpf, 1);
        addRow("E-mail:", txtEmail, 2);
        addRow("Telefone:", txtPhone, 3);
        addRow("Nascimento (dd/mm/aaaa):", txtBirthDate, 4);

        JButton btnSave = new JButton("Salvar");
        JButton btnDelete = new JButton("Excluir");
        JButton btnClear = new JButton("Limpar");
        JButton btnHistory = new JButton("Ver histórico");

        btnSave.addActionListener(e -> save());
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clearForm());
        btnHistory.addActionListener(e -> showHistory());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(btnSave);
        buttons.add(btnDelete);
        buttons.add(btnClear);
        buttons.add(btnHistory);
        addRow("", buttons, 5);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Buscar (nome, CPF, e-mail ou telefone):"));
        searchPanel.add(txtSearch);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

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
        north.add(searchPanel, BorderLayout.SOUTH);

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
        try {
            List<Guest> guests = guestService.getAllGuests();
            tableModel.setRowCount(0);
            for (Guest g : guests) {
                tableModel.addRow(new Object[]{
                        g.getId_guest(),
                        g.getName(),
                        g.getCpf(),
                        g.getEmail(),
                        g.getPhoneNumber() == null ? "" : g.getPhoneNumber(),
                        g.getBirthDate() == null ? "" : g.getBirthDate().format(DATE_FORMAT)
                });
            }
        } catch (Exception ex) {
            showError("Erro ao carregar hóspedes: " + ex.getMessage());
        }
    }

    private void save() {
        LocalDate birthDate;
        try {
            birthDate = parseDate(txtBirthDate.getText());
        } catch (DateTimeParseException ex) {
            warn("Data de nascimento inválida. Use o formato dd/mm/aaaa.");
            return;
        }

        GuestRequestDTO dto = new GuestRequestDTO(
                txtName.getText().trim(),
                txtCpf.getText().trim(),
                txtEmail.getText().trim(),
                emptyToNull(txtPhone.getText()),
                birthDate
        );

        String validationError = validate(dto);
        if (validationError != null) {
            warn(validationError);
            return;
        }

        try {
            if (selectedId == null) {
                guestService.createGuest(dto);
                showInfo("Hóspede cadastrado com sucesso!");
            } else {
                guestService.updateGuest(selectedId, dto);
                showInfo("Hóspede atualizado com sucesso!");
            }
            clearForm();
            refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro inesperado ao salvar: " + ex.getMessage());
        }
    }

    private void delete() {
        if (selectedId == null) {
            warn("Selecione um hóspede na tabela para excluir.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir este hóspede?\nSuas reservas também serão removidas.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            guestService.deleteGuest(selectedId);
            showInfo("Hóspede excluído com sucesso!");
            clearForm();
            refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Não foi possível excluir o hóspede: " + ex.getMessage());
        }
    }

    private void showHistory() {
        if (selectedId == null) {
            warn("Selecione um hóspede na tabela para ver o histórico.");
            return;
        }

        try {
            Guest guest = guestService.getGuestById(selectedId);

            List<Reservation> reservations = guest.getReservations().stream()
                    .sorted(Comparator.comparing(Reservation::getCheckInDate).reversed())
                    .toList();

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Hotel", "Quarto", "Entrada", "Saída", "Status", "Serviços", "Valor total"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            double totalSpent = 0.0;
            for (Reservation r : reservations) {
                String services = r.getConsumedServices().isEmpty()
                        ? "—"
                        : r.getConsumedServices().stream().map(Enum::name).collect(Collectors.joining(", "));

                model.addRow(new Object[]{
                        r.getRoom().getHotel().getName(),
                        r.getRoom().getNumber(),
                        r.getCheckInDate().format(DATE_FORMAT),
                        r.getCheckOutDate().format(DATE_FORMAT),
                        statusLabel(r.getStatus()),
                        services,
                        String.format(PT_BR, "R$ %.2f", r.getTotalValue())
                });

                if (r.getStatus() != ReservationStatus.CANCELED) {
                    totalSpent += r.getTotalValue();
                }
            }

            JTable historyTable = new JTable(model);
            historyTable.setAutoCreateRowSorter(true);

            String summary = reservations.isEmpty()
                    ? "Nenhuma hospedagem registrada para este hóspede."
                    : String.format(PT_BR, "%d reserva(s) | Total gasto (sem canceladas): R$ %.2f",
                    reservations.size(), totalSpent);

            JPanel content = new JPanel(new BorderLayout(8, 8));
            content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            content.add(new JLabel(summary), BorderLayout.NORTH);
            content.add(new JScrollPane(historyTable), BorderLayout.CENTER);

            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                    "Histórico de " + guest.getName(), Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setContentPane(content);
            dialog.setSize(900, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro ao carregar o histórico: " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedId = null;
        txtName.setText("");
        txtCpf.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtBirthDate.setText("");
        table.clearSelection();
        updateFormTitle();
    }

    private void fillFormFromSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        selectedId = (UUID) tableModel.getValueAt(row, 0);
        txtName.setText((String) tableModel.getValueAt(row, 1));
        txtCpf.setText((String) tableModel.getValueAt(row, 2));
        txtEmail.setText((String) tableModel.getValueAt(row, 3));
        txtPhone.setText((String) tableModel.getValueAt(row, 4));
        txtBirthDate.setText((String) tableModel.getValueAt(row, 5));
        updateFormTitle();
    }

    private void applyFilter() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2, 3, 4));
        }
    }

    private String validate(GuestRequestDTO dto) {
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

    private String emptyToNull(String text) {
        String value = text.trim();
        return value.isEmpty() ? null : value;
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

    private void updateFormTitle() {
        String title = (selectedId == null) ? "Novo hóspede" : "Editando hóspede";
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