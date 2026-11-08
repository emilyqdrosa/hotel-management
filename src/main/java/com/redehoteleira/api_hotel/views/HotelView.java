package com.redehoteleira.api_hotel.views;

import com.redehoteleira.api_hotel.dtos.HotelRequestDTO;
import com.redehoteleira.api_hotel.model.entities.Hotel;
import com.redehoteleira.api_hotel.services.HotelService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class HotelView extends JPanel {

    private final HotelService hotelService;
    private final Validator validator;

    private final JTextField txtName = new JTextField(25);
    private final JTextField txtAddress = new JTextField(25);
    private final JTextField txtPhone = new JTextField(25);
    private final JTextField txtSearch = new JTextField(20);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Endereço", "Telefone"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final JPanel formPanel = new JPanel(new GridBagLayout());

    private UUID selectedId = null;

    public HotelView(HotelService hotelService, Validator validator) {
        this.hotelService = hotelService;
        this.validator = validator;

        buildUI();
        refresh();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        updateFormTitle();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        addField("Nome:", txtName, 0, gbc);
        addField("Endereço:", txtAddress, 1, gbc);
        addField("Telefone:", txtPhone, 2, gbc);

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

        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(buttons, gbc);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Buscar por nome:"));
        searchPanel.add(txtSearch);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.removeColumn(table.getColumnModel().getColumn(0)); // esconde a coluna ID
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

    private void addField(String label, JTextField field, int row, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        formPanel.add(field, gbc);
    }

    public void refresh() {
        try {
            List<Hotel> hotels = hotelService.getAllHotels();
            tableModel.setRowCount(0);
            for (Hotel h : hotels) {
                tableModel.addRow(new Object[]{h.getId(), h.getName(), h.getAddress(), h.getPhone()});
            }
        } catch (Exception ex) {
            showError("Erro ao carregar hotéis: " + ex.getMessage());
        }
    }

    private void save() {
        HotelRequestDTO dto = new HotelRequestDTO(
                txtName.getText().trim(),
                txtAddress.getText().trim(),
                txtPhone.getText().trim()
        );

        String validationError = validate(dto);
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError, "Dados inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (selectedId == null) {
                hotelService.createHotel(dto);
                showInfo("Hotel cadastrado com sucesso!");
            } else {
                hotelService.updateHotel(selectedId, dto);
                showInfo("Hotel atualizado com sucesso!");
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
            JOptionPane.showMessageDialog(this, "Selecione um hotel na tabela para excluir.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir este hotel?\nQuartos e funcionários vinculados também serão removidos.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            hotelService.deleteHotel(selectedId);
            showInfo("Hotel excluído com sucesso!");
            clearForm();
            refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Não foi possível excluir o hotel: " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedId = null;
        txtName.setText("");
        txtAddress.setText("");
        txtPhone.setText("");
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
        txtAddress.setText((String) tableModel.getValueAt(row, 2));
        txtPhone.setText((String) tableModel.getValueAt(row, 3));
        updateFormTitle();
    }

    private void applyFilter() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
        }
    }

    private String validate(HotelRequestDTO dto) {
        Set<ConstraintViolation<HotelRequestDTO>> violations = validator.validate(dto);
        if (violations.isEmpty()) {
            return null;
        }
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    private void updateFormTitle() {
        String title = (selectedId == null) ? "Novo hotel" : "Editando hotel";
        formPanel.setBorder(BorderFactory.createTitledBorder(title));
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}