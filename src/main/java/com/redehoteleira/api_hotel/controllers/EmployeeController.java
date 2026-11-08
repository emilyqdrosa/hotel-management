package com.redehoteleira.api_hotel.controllers;

import com.redehoteleira.api_hotel.dtos.AdministratorRequestDTO;
import com.redehoteleira.api_hotel.dtos.AdministratorResponseDTO;
import com.redehoteleira.api_hotel.dtos.EmployeeRequestDTO;
import com.redehoteleira.api_hotel.dtos.EmployeeResponseDTO;
import com.redehoteleira.api_hotel.dtos.ManagerRequestDTO;
import com.redehoteleira.api_hotel.dtos.ManagerResponseDTO;
import com.redehoteleira.api_hotel.dtos.ReceptionistRequestDTO;
import com.redehoteleira.api_hotel.dtos.ReceptionistResponseDTO;
import com.redehoteleira.api_hotel.model.entities.Administrator;
import com.redehoteleira.api_hotel.model.entities.Employee;
import com.redehoteleira.api_hotel.model.entities.Manager;
import com.redehoteleira.api_hotel.model.entities.Receptionist;
import com.redehoteleira.api_hotel.services.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@RequestBody @Valid EmployeeRequestDTO data) {
        Employee employee = employeeService.createEmployee(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(employee));
    }

    @PostMapping("/receptionists")
    public ResponseEntity<ReceptionistResponseDTO> createReceptionist(@RequestBody @Valid ReceptionistRequestDTO data) {
        Receptionist receptionist = employeeService.createReceptionist(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(toReceptionistResponse(receptionist));
    }

    @PostMapping("/managers")
    public ResponseEntity<ManagerResponseDTO> createManager(@RequestBody @Valid ManagerRequestDTO data) {
        Manager manager = employeeService.createManager(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(toManagerResponse(manager));
    }

    @PostMapping("/administrators")
    public ResponseEntity<AdministratorResponseDTO> createAdministrator(@RequestBody @Valid AdministratorRequestDTO data) {
        Administrator administrator = employeeService.createAdministrator(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAdministratorResponse(administrator));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> findAll(@RequestParam(required = false) UUID hotelId) {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees()
                .stream()
                .filter(e -> hotelId == null || e.getHotel().getId().equals(hotelId))
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable UUID id) {
        Employee employee = employeeService.getEmployeeById(id);

        if (employee instanceof Receptionist receptionist) {
            return ResponseEntity.ok(toReceptionistResponse(receptionist));
        }
        if (employee instanceof Manager manager) {
            return ResponseEntity.ok(toManagerResponse(manager));
        }
        if (employee instanceof Administrator administrator) {
            return ResponseEntity.ok(toAdministratorResponse(administrator));
        }
        return ResponseEntity.ok(toResponse(employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(@PathVariable UUID id,
                                                      @RequestBody @Valid EmployeeRequestDTO data) {
        return ResponseEntity.ok(toResponse(employeeService.updateEmployee(id, data)));
    }

    @PutMapping("/receptionists/{id}")
    public ResponseEntity<ReceptionistResponseDTO> updateReceptionist(@PathVariable UUID id,
                                                                      @RequestBody @Valid ReceptionistRequestDTO data) {
        return ResponseEntity.ok(toReceptionistResponse(employeeService.updateReceptionist(id, data)));
    }

    @PutMapping("/managers/{id}")
    public ResponseEntity<ManagerResponseDTO> updateManager(@PathVariable UUID id,
                                                            @RequestBody @Valid ManagerRequestDTO data) {
        return ResponseEntity.ok(toManagerResponse(employeeService.updateManager(id, data)));
    }

    @PutMapping("/administrators/{id}")
    public ResponseEntity<AdministratorResponseDTO> updateAdministrator(@PathVariable UUID id,
                                                                        @RequestBody @Valid AdministratorRequestDTO data) {
        return ResponseEntity.ok(toAdministratorResponse(employeeService.updateAdministrator(id, data)));
    }

    @PatchMapping("/{id}/vacation/start")
    public ResponseEntity<EmployeeResponseDTO> startVacation(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(employeeService.startVacation(id)));
    }

    @PatchMapping("/{id}/vacation/end")
    public ResponseEntity<EmployeeResponseDTO> endVacation(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(employeeService.endVacation(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    private UUID workShiftId(Employee employee) {
        return null;
    }

    private EmployeeResponseDTO toResponse(Employee e) {
        return new EmployeeResponseDTO(
                e.getId_employee(),
                e.getHotel().getId(),
                e.getName(),
                e.getCpf(),
                e.getSalary(),
                e.getHireDate(),
                workShiftId(e),
                e.getIsOnVacation()
        );
    }

    private ReceptionistResponseDTO toReceptionistResponse(Receptionist e) {
        return new ReceptionistResponseDTO(
                e.getId_employee(),
                e.getHotel().getId(),
                e.getName(),
                e.getCpf(),
                e.getSalary(),
                e.getHireDate(),
                workShiftId(e),
                e.getIsOnVacation(),
                e.getSpokenLanguages()
        );
    }

    private ManagerResponseDTO toManagerResponse(Manager e) {
        return new ManagerResponseDTO(
                e.getId_employee(),
                e.getHotel().getId(),
                e.getName(),
                e.getCpf(),
                e.getSalary(),
                e.getHireDate(),
                workShiftId(e),
                e.getIsOnVacation(),
                e.getDepartment()
        );
    }

    private AdministratorResponseDTO toAdministratorResponse(Administrator e) {
        return new AdministratorResponseDTO(
                e.getId_employee(),
                e.getHotel().getId(),
                e.getName(),
                e.getCpf(),
                e.getSalary(),
                e.getHireDate(),
                workShiftId(e),
                e.getIsOnVacation(),
                e.getSystemAccessLevel()
        );
    }
}