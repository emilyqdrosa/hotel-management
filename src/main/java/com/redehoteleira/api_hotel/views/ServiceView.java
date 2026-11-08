package com.redehoteleira.api_hotel.views;

import com.redehoteleira.api_hotel.dtos.ServiceRequestDTO;
import com.redehoteleira.api_hotel.model.entities.Service;
import com.redehoteleira.api_hotel.model.enums.ServiceType;
import com.redehoteleira.api_hotel.services.ServiceService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class ServiceView extends JPanel {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final ServiceService serviceService;
    private final Validator validator;

    private final JComboBox<ServiceType> cbType = new JComboBox<>(ServiceType.values());
    private final JTextField txtName = new JTextField(25);
    private final JTextField txtPrice = new JTextField(25);
    private final JPanel formPanel = new JPanel(new GridBagLayout());

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Tipo", "Nome", "Preço"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final Map<UUID, Service> servicesById = new HashMap<>();
    private UUID selectedId = null;

    public ServiceView(ServiceService serviceService, Validator validator) {
        this.serviceService = serviceService;
        this.validator = validator;

        buildUI();
        refresh();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        updateFormTitle();
        cbType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ServiceType type) {
                    setText(typeLabel(type));
                }
                return this;
            }
        });
        cbType.addActionListener(e -> {
            if (selectedId == null) {
                fillDefaultPrice();
            }
        });

        addRow("Tipo:", cbType, 0);
        addRow("Nome (opcional):", txtName, 1);
        addRow("Preço (R$):", txtPrice, 2);

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
        addRow("", buttons, 3);

        fillDefaultPrice();

        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.removeColumn(table.getColumnModel().getColumn(0)); // esconde ID
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
            List<Service> services = serviceService.getAllServices();

            servicesById.clear();
            tableModel.setRowCount(0);
            for (Service s : services) {
                servicesById.put(s.getId_service(), s);
                tableModel.addRow(new Object[]{
                        s.getId_service(),
                        typeLabel(s.getType()),
                        s.getName(),
                        String.format(PT_BR, "R$ %.2f", s.getPrice())
                });
            }
        } catch (Exception ex) {
            showError("Erro ao carregar serviços: " + ex.getMessage());
        }
    }

    private void save() {
        Double price;
        try {
            price = parseDouble(txtPrice.getText());
        } catch (NumberFormatException ex) {
            warn("O preço deve ser um número válido.");
            return;
        }

        ServiceRequestDTO dto = new ServiceRequestDTO(
                (ServiceType) cbType.getSelectedItem(),
                emptyToNull(txtName.getText()),
                price
        );

        String validationError = validate(dto);
        if (validationError != null) {
            warn(validationError);
            return;
        }

        try {
            if (selectedId == null) {
                serviceService.createService(dto);
                showInfo("Serviço cadastrado com sucesso!");
            } else {
                serviceService.updateService(selectedId, dto);
                showInfo("Serviço atualizado com sucesso!");
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
            warn("Selecione um serviço na tabela para excluir.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir este serviço?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            serviceService.deleteService(selectedId);
            showInfo("Serviço excluído com sucesso!");
            clearForm();
            refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Não foi possível excluir o serviço: " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedId = null;
        txtName.setText("");
        cbType.setSelectedIndex(0);
        table.clearSelection();
        fillDefaultPrice();
        updateFormTitle();
    }

    private void fillFormFromSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        UUID id = (UUID) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
        Service service = servicesById.get(id);
        if (service == null) {
            return;
        }

        selectedId = id;
        cbType.setSelectedItem(service.getType());
        txtName.setText(service.getName());
        txtPrice.setText(String.valueOf(service.getPrice()));
        updateFormTitle();
    }

    private void fillDefaultPrice() {
        ServiceType type = (ServiceType) cbType.getSelectedItem();
        txtPrice.setText(type == null ? "" : String.valueOf(type.getDefaultPrice()));
    }

    private String validate(ServiceRequestDTO dto) {
        String errors = validator.validate(dto).stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining("\n"));
        return errors.isEmpty() ? null : errors;
    }

    private Double parseDouble(String text) {
        String value = text.trim().replace(',', '.');
        return value.isEmpty() ? null : Double.valueOf(value);
    }

    private String emptyToNull(String text) {
        String value = text.trim();
        return value.isEmpty() ? null : value;
    }

    private static String typeLabel(ServiceType type) {
        if (type == null) return "";
        return switch (type) {
            case ROOM_SERVICE -> "Serviço de quarto";
            case LAUNDRY -> "Lavanderia";
            case BREAKFAST -> "Café da manhã";
            case PARKING -> "Estacionamento";
        };
    }

    private void updateFormTitle() {
        String title = (selectedId == null) ? "Novo serviço" : "Editando serviço";
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