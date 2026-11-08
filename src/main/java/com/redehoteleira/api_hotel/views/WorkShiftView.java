package com.redehoteleira.api_hotel.views;

import com.redehoteleira.api_hotel.dtos.WorkShiftRequestDTO;
import com.redehoteleira.api_hotel.model.entities.WorkShift;
import com.redehoteleira.api_hotel.services.WorkShiftService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class WorkShiftView extends JPanel {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private static final String[] DAY_CODES = {"SEG", "TER", "QUA", "QUI", "SEX", "SAB", "DOM"};
    private static final String[] DAY_LABELS = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};

    private final WorkShiftService workShiftService;
    private final Validator validator;

    private final JTextField txtDescription = new JTextField(25);
    private final JTextField txtStartTime = new JTextField(8);
    private final JTextField txtEndTime = new JTextField(8);
    private final JCheckBox[] dayChecks = new JCheckBox[DAY_CODES.length];
    private final JPanel formPanel = new JPanel(new GridBagLayout());

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Descrição", "Início", "Término", "Dias de trabalho"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private UUID selectedId = null;

    public WorkShiftView(WorkShiftService workShiftService, Validator validator) {
        this.workShiftService = workShiftService;
        this.validator = validator;

        buildUI();
        refresh();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        updateFormTitle();

        JPanel daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        for (int i = 0; i < DAY_CODES.length; i++) {
            dayChecks[i] = new JCheckBox(DAY_LABELS[i]);
            daysPanel.add(dayChecks[i]);
        }

        addRow("Descrição:", txtDescription, 0);
        addRow("Início (HH:mm):", txtStartTime, 1);
        addRow("Término (HH:mm):", txtEndTime, 2);
        addRow("Dias de trabalho:", daysPanel, 3);

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
        addRow("", buttons, 4);

        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });

        add(formPanel, BorderLayout.NORTH);
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
            List<WorkShift> workShifts = workShiftService.getAllWorkShifts();
            tableModel.setRowCount(0);
            for (WorkShift ws : workShifts) {
                tableModel.addRow(new Object[]{
                        ws.getId_work_shift(),
                        ws.getDescription(),
                        ws.getStartTime().format(TIME_FORMAT),
                        ws.getEndTime().format(TIME_FORMAT),
                        ws.getWorkingDays().replace(",", ", ")
                });
            }
        } catch (Exception ex) {
            showError("Erro ao carregar turnos: " + ex.getMessage());
        }
    }

    private void save() {
        LocalTime start;
        LocalTime end;
        try {
            start = parseTime(txtStartTime.getText());
            end = parseTime(txtEndTime.getText());
        } catch (DateTimeParseException ex) {
            warn("Horário inválido. Use o formato HH:mm (ex.: 08:00).");
            return;
        }

        WorkShiftRequestDTO dto = new WorkShiftRequestDTO(
                txtDescription.getText().trim(),
                start,
                end,
                selectedDays()
        );

        String validationError = validate(dto);
        if (validationError != null) {
            warn(validationError);
            return;
        }

        try {
            if (selectedId == null) {
                workShiftService.createWorkShift(dto);
                showInfo("Turno cadastrado com sucesso!");
            } else {
                workShiftService.updateWorkShift(selectedId, dto);
                showInfo("Turno atualizado com sucesso!");
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
            warn("Selecione um turno na tabela para excluir.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir este turno?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            workShiftService.deleteWorkShift(selectedId);
            showInfo("Turno excluído com sucesso!");
            clearForm();
            refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Não foi possível excluir o turno. Ele pode estar vinculado a funcionários.");
        }
    }

    private void clearForm() {
        selectedId = null;
        txtDescription.setText("");
        txtStartTime.setText("");
        txtEndTime.setText("");
        for (JCheckBox check : dayChecks) {
            check.setSelected(false);
        }
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
        txtDescription.setText((String) tableModel.getValueAt(row, 1));
        txtStartTime.setText((String) tableModel.getValueAt(row, 2));
        txtEndTime.setText((String) tableModel.getValueAt(row, 3));

        String days = ((String) tableModel.getValueAt(row, 4)).replace(" ", "");
        List<String> selected = Arrays.asList(days.split(","));
        for (int i = 0; i < DAY_CODES.length; i++) {
            dayChecks[i].setSelected(selected.contains(DAY_CODES[i]));
        }

        updateFormTitle();
    }

    private String selectedDays() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DAY_CODES.length; i++) {
            if (dayChecks[i].isSelected()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(DAY_CODES[i]);
            }
        }
        return sb.toString();
    }

    private LocalTime parseTime(String text) {
        String value = text.trim();
        return value.isEmpty() ? null : LocalTime.parse(value, TIME_FORMAT);
    }

    private String validate(WorkShiftRequestDTO dto) {
        String errors = validator.validate(dto).stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining("\n"));
        return errors.isEmpty() ? null : errors;
    }

    private void updateFormTitle() {
        String title = (selectedId == null) ? "Novo turno" : "Editando turno";
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