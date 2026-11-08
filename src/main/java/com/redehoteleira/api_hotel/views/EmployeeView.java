package com.redehoteleira.api_hotel.views;

import com.redehoteleira.api_hotel.dtos.AdministratorRequestDTO;
import com.redehoteleira.api_hotel.dtos.EmployeeRequestDTO;
import com.redehoteleira.api_hotel.dtos.ManagerRequestDTO;
import com.redehoteleira.api_hotel.dtos.ReceptionistRequestDTO;
import com.redehoteleira.api_hotel.model.entities.Administrator;
import com.redehoteleira.api_hotel.model.entities.Employee;
import com.redehoteleira.api_hotel.model.entities.Hotel;
import com.redehoteleira.api_hotel.model.entities.Manager;
import com.redehoteleira.api_hotel.model.entities.Receptionist;
import com.redehoteleira.api_hotel.model.entities.WorkShift;
import com.redehoteleira.api_hotel.model.enums.SystemAccessLevel;
import com.redehoteleira.api_hotel.services.EmployeeService;
import com.redehoteleira.api_hotel.services.HotelService;
import com.redehoteleira.api_hotel.services.WorkShiftService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class EmployeeView extends JPanel {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final int TYPE_EMPLOYEE = 0;
    private static final int TYPE_RECEPTIONIST = 1;
    private static final int TYPE_MANAGER = 2;
    private static final int TYPE_ADMINISTRATOR = 3;

    private record HotelItem(UUID id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record WorkShiftItem(UUID id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private final EmployeeService employeeService;
    private final HotelService hotelService;
    private final WorkShiftService workShiftService;
    private final Validator validator;

    // Formulário
    private final JComboBox<HotelItem> cbHotel = new JComboBox<>();
    private final JComboBox<String> cbType = new JComboBox<>(
            new String[]{"Funcionário", "Recepcionista", "Gerente", "Administrador"});
    private final JTextField txtName = new JTextField(20);
    private final JTextField txtCpf = new JTextField(20);
    private final JTextField txtSalary = new JTextField(20);
    private final JTextField txtHireDate = new JTextField(20);
    private final JComboBox<WorkShiftItem> cbWorkShift = new JComboBox<>();
    private final JTextField txtLanguages = new JTextField(20);
    private final JTextField txtDepartment = new JTextField(20);
    private final JComboBox<SystemAccessLevel> cbAccessLevel = new JComboBox<>(SystemAccessLevel.values());
    private final CardLayout extraLayout = new CardLayout();
    private final JPanel extraPanel = new JPanel(extraLayout);
    private final JPanel formPanel = new JPanel(new GridBagLayout());

    // Filtro
    private final JComboBox<HotelItem> cbFilterHotel = new JComboBox<>();

    // Tabela
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Hotel", "Nome", "CPF", "Cargo", "Salário", "Contratação",
                    "Bônus", "Férias", "Extra", "Turno"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final Map<UUID, Employee> employeesById = new HashMap<>();
    private UUID selectedId = null;
    private boolean loading = false;

    public EmployeeView(EmployeeService employeeService, HotelService hotelService,
                        WorkShiftService workShiftService, Validator validator) {
        this.employeeService = employeeService;
        this.hotelService = hotelService;
        this.workShiftService = workShiftService;
        this.validator = validator;

        buildUI();
        refresh();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---------- Painel de campos extras (por cargo) ----------
        extraPanel.add(new JPanel(), "NONE");
        extraPanel.add(labeled("Idiomas:", txtLanguages), "REC");
        extraPanel.add(labeled("Departamento:", txtDepartment), "MGR");
        extraPanel.add(labeled("Nível de acesso:", cbAccessLevel), "ADM");

        cbAccessLevel.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SystemAccessLevel level) {
                    setText(accessLabel(level));
                }
                return this;
            }
        });
        cbType.addActionListener(e -> updateExtraFields());

        updateFormTitle();
        addRow("Hotel:", cbHotel, 0);
        addRow("Cargo:", cbType, 1);
        addRow("Nome:", txtName, 2);
        addRow("CPF:", txtCpf, 3);
        addRow("Salário (R$):", txtSalary, 4);
        addRow("Contratação (dd/mm/aaaa):", txtHireDate, 5);
        addRow("Turno:", cbWorkShift, 6);
        addRow("", extraPanel, 7);

        JButton btnSave = new JButton("Salvar");
        JButton btnDelete = new JButton("Excluir");
        JButton btnClear = new JButton("Limpar");
        JButton btnStartVacation = new JButton("Iniciar férias");
        JButton btnEndVacation = new JButton("Encerrar férias");

        btnSave.addActionListener(e -> save());
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clearForm());
        btnStartVacation.addActionListener(e -> changeVacation(true));
        btnEndVacation.addActionListener(e -> changeVacation(false));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(btnSave);
        buttons.add(btnDelete);
        buttons.add(btnClear);
        buttons.add(btnStartVacation);
        buttons.add(btnEndVacation);
        addRow("", buttons, 8);

        updateExtraFields();

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filtrar por hotel:"));
        filterPanel.add(cbFilterHotel);
        cbFilterHotel.addActionListener(e -> {
            if (!loading) loadEmployees();
        });

        table.setAutoCreateRowSorter(true);
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

    private JPanel labeled(String label, Component component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(new JLabel(label + " "));
        panel.add(component);
        return panel;
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

    // ---------- Carregamento ----------

    public void refresh() {
        loading = true;
        try {
            loadHotels();
            loadWorkShifts();
        } catch (Exception ex) {
            showError("Erro ao carregar dados: " + ex.getMessage());
        } finally {
            loading = false;
        }
        loadEmployees();
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

    private void loadWorkShifts() {
        WorkShiftItem previous = (WorkShiftItem) cbWorkShift.getSelectedItem();

        DefaultComboBoxModel<WorkShiftItem> model = new DefaultComboBoxModel<>();
        model.addElement(new WorkShiftItem(null, "Não definido"));
        for (WorkShift ws : workShiftService.getAllWorkShifts()) {
            model.addElement(new WorkShiftItem(ws.getId_work_shift(), shiftLabel(ws)));
        }
        cbWorkShift.setModel(model);
        cbWorkShift.setSelectedIndex(0);

        if (previous != null) selectWorkShift(previous.id());
    }

    private void loadEmployees() {
        try {
            HotelItem filter = (HotelItem) cbFilterHotel.getSelectedItem();

            List<Employee> employees = employeeService.getAllEmployees().stream()
                    .filter(e -> filter == null || filter.id() == null || e.getHotel().getId().equals(filter.id()))
                    .toList();

            employeesById.clear();
            tableModel.setRowCount(0);
            for (Employee e : employees) {
                employeesById.put(e.getId_employee(), e);
                tableModel.addRow(new Object[]{
                        e.getId_employee(),
                        e.getHotel().getName(),
                        e.getName(),
                        e.getCpf(),
                        typeLabel(e),
                        money(e.getSalary()),
                        e.getHireDate().format(DATE_FORMAT),
                        money(e.calculateBonus()),
                        Boolean.TRUE.equals(e.getIsOnVacation()) ? "Sim" : "Não",
                        extraLabel(e),
                        e.getWorkShift() == null ? "Não definido" : shiftLabel(e.getWorkShift())
                });
            }
        } catch (Exception ex) {
            showError("Erro ao carregar funcionários: " + ex.getMessage());
        }
    }

    // ---------- Ações ----------

    private void save() {
        HotelItem hotel = (HotelItem) cbHotel.getSelectedItem();
        UUID hotelId = hotel == null ? null : hotel.id();

        Double salary;
        LocalDate hireDate;
        try {
            salary = parseDouble(txtSalary.getText());
            hireDate = parseDate(txtHireDate.getText());
        } catch (NumberFormatException ex) {
            warn("O salário deve ser um número válido.");
            return;
        } catch (DateTimeParseException ex) {
            warn("Data de contratação inválida. Use o formato dd/mm/aaaa.");
            return;
        }

        String name = txtName.getText().trim();
        String cpf = txtCpf.getText().trim();
        int type = cbType.getSelectedIndex();
        UUID workShiftId = selectedWorkShiftId();

        Object dto = switch (type) {
            case TYPE_RECEPTIONIST -> new ReceptionistRequestDTO(hotelId, name, cpf, salary, hireDate, workShiftId,
                    emptyToNull(txtLanguages.getText()));
            case TYPE_MANAGER -> new ManagerRequestDTO(hotelId, name, cpf, salary, hireDate, workShiftId,
                    emptyToNull(txtDepartment.getText()));
            case TYPE_ADMINISTRATOR -> new AdministratorRequestDTO(hotelId, name, cpf, salary, hireDate, workShiftId,
                    (SystemAccessLevel) cbAccessLevel.getSelectedItem());
            default -> new EmployeeRequestDTO(hotelId, name, cpf, salary, hireDate, workShiftId);
        };

        String validationError = validate(dto);
        if (validationError != null) {
            warn(validationError);
            return;
        }

        try {
            if (selectedId == null) {
                switch (type) {
                    case TYPE_RECEPTIONIST -> employeeService.createReceptionist((ReceptionistRequestDTO) dto);
                    case TYPE_MANAGER -> employeeService.createManager((ManagerRequestDTO) dto);
                    case TYPE_ADMINISTRATOR -> employeeService.createAdministrator((AdministratorRequestDTO) dto);
                    default -> employeeService.createEmployee((EmployeeRequestDTO) dto);
                }
                showInfo("Funcionário cadastrado com sucesso!");
            } else {
                switch (type) {
                    case TYPE_RECEPTIONIST -> employeeService.updateReceptionist(selectedId, (ReceptionistRequestDTO) dto);
                    case TYPE_MANAGER -> employeeService.updateManager(selectedId, (ManagerRequestDTO) dto);
                    case TYPE_ADMINISTRATOR -> employeeService.updateAdministrator(selectedId, (AdministratorRequestDTO) dto);
                    default -> employeeService.updateEmployee(selectedId, (EmployeeRequestDTO) dto);
                }
                showInfo("Funcionário atualizado com sucesso!");
            }
            clearForm();
            loadEmployees();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro inesperado ao salvar: " + ex.getMessage());
        }
    }

    private void delete() {
        if (selectedId == null) {
            warn("Selecione um funcionário na tabela para excluir.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir este funcionário?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            employeeService.deleteEmployee(selectedId);
            showInfo("Funcionário excluído com sucesso!");
            clearForm();
            loadEmployees();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Não foi possível excluir o funcionário: " + ex.getMessage());
        }
    }

    private void changeVacation(boolean start) {
        if (selectedId == null) {
            warn("Selecione um funcionário na tabela.");
            return;
        }

        try {
            if (start) {
                employeeService.startVacation(selectedId);
                showInfo("Férias iniciadas.");
            } else {
                employeeService.endVacation(selectedId);
                showInfo("Férias encerradas.");
            }
            clearForm();
            loadEmployees();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro ao alterar férias: " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedId = null;
        txtName.setText("");
        txtCpf.setText("");
        txtSalary.setText("");
        txtHireDate.setText("");
        txtLanguages.setText("");
        txtDepartment.setText("");
        cbAccessLevel.setSelectedIndex(0);
        cbWorkShift.setSelectedIndex(0);
        cbType.setSelectedIndex(0);
        cbType.setEnabled(true);
        table.clearSelection();
        updateExtraFields();
        updateFormTitle();
    }

    private void fillFormFromSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        UUID id = (UUID) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
        Employee employee = employeesById.get(id);
        if (employee == null) {
            return;
        }

        selectedId = id;
        selectHotel(cbHotel, employee.getHotel().getId());
        selectWorkShift(employee.getWorkShift() == null ? null : employee.getWorkShift().getId_work_shift());
        txtName.setText(employee.getName());
        txtCpf.setText(employee.getCpf());
        txtSalary.setText(String.valueOf(employee.getSalary()));
        txtHireDate.setText(employee.getHireDate().format(DATE_FORMAT));

        txtLanguages.setText("");
        txtDepartment.setText("");
        cbAccessLevel.setSelectedIndex(0);

        if (employee instanceof Receptionist receptionist) {
            cbType.setSelectedIndex(TYPE_RECEPTIONIST);
            txtLanguages.setText(receptionist.getSpokenLanguages() == null ? "" : receptionist.getSpokenLanguages());
        } else if (employee instanceof Manager manager) {
            cbType.setSelectedIndex(TYPE_MANAGER);
            txtDepartment.setText(manager.getDepartment() == null ? "" : manager.getDepartment());
        } else if (employee instanceof Administrator administrator) {
            cbType.setSelectedIndex(TYPE_ADMINISTRATOR);
            cbAccessLevel.setSelectedItem(administrator.getSystemAccessLevel());
        } else {
            cbType.setSelectedIndex(TYPE_EMPLOYEE);
        }

        cbType.setEnabled(false); // o cargo não pode ser alterado na edição
        updateExtraFields();
        updateFormTitle();
    }

    // ---------- Utilitários ----------

    private void selectHotel(JComboBox<HotelItem> combo, UUID id) {
        ComboBoxModel<HotelItem> model = combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (Objects.equals(model.getElementAt(i).id(), id)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectWorkShift(UUID id) {
        ComboBoxModel<WorkShiftItem> model = cbWorkShift.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (Objects.equals(model.getElementAt(i).id(), id)) {
                cbWorkShift.setSelectedIndex(i);
                return;
            }
        }
        cbWorkShift.setSelectedIndex(0);
    }

    private UUID selectedWorkShiftId() {
        WorkShiftItem item = (WorkShiftItem) cbWorkShift.getSelectedItem();
        return item == null ? null : item.id();
    }

    private static String shiftLabel(WorkShift ws) {
        return ws.getDescription() + " (" + ws.getStartTime().format(TIME_FORMAT)
                + " - " + ws.getEndTime().format(TIME_FORMAT) + ")";
    }

    private void updateExtraFields() {
        String card = switch (cbType.getSelectedIndex()) {
            case TYPE_RECEPTIONIST -> "REC";
            case TYPE_MANAGER -> "MGR";
            case TYPE_ADMINISTRATOR -> "ADM";
            default -> "NONE";
        };
        extraLayout.show(extraPanel, card);
    }

    private void updateFormTitle() {
        String title = (selectedId == null) ? "Novo funcionário" : "Editando funcionário";
        formPanel.setBorder(BorderFactory.createTitledBorder(title));
    }

    private String validate(Object dto) {
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

    private LocalDate parseDate(String text) {
        String value = text.trim();
        return value.isEmpty() ? null : LocalDate.parse(value, DATE_FORMAT);
    }

    private String emptyToNull(String text) {
        String value = text.trim();
        return value.isEmpty() ? null : value;
    }

    private static String money(Double value) {
        return value == null ? "" : String.format(PT_BR, "R$ %.2f", value);
    }

    private static String typeLabel(Employee e) {
        if (e instanceof Receptionist) return "Recepcionista";
        if (e instanceof Manager) return "Gerente";
        if (e instanceof Administrator) return "Administrador";
        return "Funcionário";
    }

    private static String extraLabel(Employee e) {
        if (e instanceof Receptionist r) {
            return r.getSpokenLanguages() == null ? "" : "Idiomas: " + r.getSpokenLanguages();
        }
        if (e instanceof Manager m) {
            return m.getDepartment() == null ? "" : "Depto: " + m.getDepartment();
        }
        if (e instanceof Administrator a) {
            return "Acesso: " + accessLabel(a.getSystemAccessLevel());
        }
        return "";
    }

    private static String accessLabel(SystemAccessLevel level) {
        if (level == null) return "";
        return switch (level) {
            case TOTAL -> "Total";
            case RESTRICTED -> "Restrito";
            case READ_ONLY -> "Somente leitura";
        };
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