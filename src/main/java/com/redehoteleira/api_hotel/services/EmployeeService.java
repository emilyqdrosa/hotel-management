package com.redehoteleira.api_hotel.services;

import com.redehoteleira.api_hotel.dtos.*;
import com.redehoteleira.api_hotel.model.entities.*;
import com.redehoteleira.api_hotel.repositories.EmployeeRepository;
import com.redehoteleira.api_hotel.repositories.HotelRepository;
import com.redehoteleira.api_hotel.repositories.WorkShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final HotelRepository hotelRepository;
    private final WorkShiftRepository workShiftRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           HotelRepository hotelRepository,
                           WorkShiftRepository workShiftRepository) {
        this.employeeRepository = employeeRepository;
        this.hotelRepository = hotelRepository;
        this.workShiftRepository = workShiftRepository;
    }

    @Transactional
    public Employee createEmployee(EmployeeRequestDTO data) {
        validateCpf(data.cpf());
        Hotel hotel = getHotel(data.id_hotel());
        WorkShift workShift = getWorkShift(data.id_work_shift());

        Employee employee = new Employee(null, hotel, data.name(), data.cpf(), data.salary(), data.hireDate(), workShift);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Receptionist createReceptionist(ReceptionistRequestDTO data) {
        validateCpf(data.cpf());
        Hotel hotel = getHotel(data.id_hotel());
        WorkShift workShift = getWorkShift(data.id_work_shift());

        Receptionist receptionist = new Receptionist(null, hotel, data.name(), data.cpf(), data.salary(), data.hireDate(), workShift, data.spokenLanguages());
        return employeeRepository.save(receptionist);
    }

    @Transactional
    public Manager createManager(ManagerRequestDTO data) {
        validateCpf(data.cpf());
        Hotel hotel = getHotel(data.id_hotel());
        WorkShift workShift = getWorkShift(data.id_work_shift());

        Manager manager = new Manager(null, hotel, data.name(), data.cpf(), data.salary(), data.hireDate(), workShift, data.department());
        return employeeRepository.save(manager);
    }

    @Transactional
    public Administrator createAdministrator(AdministratorRequestDTO data) {
        validateCpf(data.cpf());
        Hotel hotel = getHotel(data.id_hotel());
        WorkShift workShift = getWorkShift(data.id_work_shift());

        Administrator administrator = new Administrator(null, hotel, data.name(), data.cpf(), data.salary(), data.hireDate(), workShift, data.systemAccessLevel());
        return employeeRepository.save(administrator);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado."));
    }

    @Transactional
    public Employee updateEmployee(UUID id, EmployeeRequestDTO data) {
        Employee employee = getEmployeeById(id);
        validateCpfForUpdate(employee, data.cpf());

        updateBaseData(employee, data.id_hotel(), data.name(), data.cpf(), data.salary(), data.hireDate(), data.id_work_shift());
        return employeeRepository.save(employee);
    }

    @Transactional
    public Receptionist updateReceptionist(UUID id, ReceptionistRequestDTO data) {
        Employee employee = getEmployeeById(id);
        if (!(employee instanceof Receptionist)) {
            throw new IllegalArgumentException("O funcionário informado não é um Recepcionista.");
        }

        Receptionist receptionist = (Receptionist) employee;
        validateCpfForUpdate(receptionist, data.cpf());
        updateBaseData(receptionist, data.id_hotel(), data.name(), data.cpf(), data.salary(), data.hireDate(), data.id_work_shift());

        receptionist.setSpokenLanguages(data.spokenLanguages());
        return employeeRepository.save(receptionist);
    }

    @Transactional
    public Manager updateManager(UUID id, ManagerRequestDTO data) {
        Employee employee = getEmployeeById(id);
        if (!(employee instanceof Manager)) {
            throw new IllegalArgumentException("O funcionário informado não é um Gerente.");
        }

        Manager manager = (Manager) employee;
        validateCpfForUpdate(manager, data.cpf());
        updateBaseData(manager, data.id_hotel(), data.name(), data.cpf(), data.salary(), data.hireDate(), data.id_work_shift());

        manager.setDepartment(data.department());
        return employeeRepository.save(manager);
    }

    @Transactional
    public Administrator updateAdministrator(UUID id, AdministratorRequestDTO data) {
        Employee employee = getEmployeeById(id);
        if (!(employee instanceof Administrator)) {
            throw new IllegalArgumentException("O funcionário informado não é um Administrador.");
        }

        Administrator administrator = (Administrator) employee;
        validateCpfForUpdate(administrator, data.cpf());
        updateBaseData(administrator, data.id_hotel(), data.name(), data.cpf(), data.salary(), data.hireDate(), data.id_work_shift());

        administrator.setSystemAccessLevel(data.systemAccessLevel());
        return employeeRepository.save(administrator);
    }

    @Transactional
    public Employee startVacation(UUID id) {
        Employee employee = getEmployeeById(id);
        employee.startVacation();
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee endVacation(UUID id) {
        Employee employee = getEmployeeById(id);
        employee.endVacation();
        return employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }

    private void validateCpf(String cpf) {
        if (employeeRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("Já existe um funcionário cadastrado com este CPF.");
        }
    }

    private void validateCpfForUpdate(Employee employee, String newCpf) {
        if (!employee.getCpf().equals(newCpf) && employeeRepository.existsByCpf(newCpf)) {
            throw new IllegalArgumentException("Já existe outro funcionário cadastrado com este CPF.");
        }
    }

    private Hotel getHotel(UUID hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Hotel não encontrado."));
    }

    private WorkShift getWorkShift(UUID workShiftId) {
        if (workShiftId == null) return null;
        return workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new IllegalArgumentException("Turno de trabalho não encontrado."));
    }

    private void updateBaseData(Employee employee, UUID hotelId, String name, String cpf, Double salary, LocalDate hireDate, UUID workShiftId) {
        employee.setHotel(getHotel(hotelId));
        employee.setName(name);
        employee.setCpf(cpf);
        employee.setSalary(salary);
        employee.setHireDate(hireDate);
        employee.setWorkShift(getWorkShift(workShiftId));
    }
}