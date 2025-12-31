package nl.rubium.efteling.park.boundary;

import java.util.List;
import java.util.UUID;
import nl.rubium.efteling.park.control.EmployeeControl;
import nl.rubium.efteling.park.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("api/v1/employees")
public class EmployeeBoundary {

    private final EmployeeControl employeeControl;

    @Autowired
    public EmployeeBoundary(EmployeeControl employeeControl) {
        this.employeeControl = employeeControl;
    }

    @GetMapping
    public List<org.openapitools.client.model.EmployeeDto> getEmployees() {
        return employeeControl.getAllEmployees().stream().map(Employee::toDto).toList();
    }

    @GetMapping("{id}")
    public org.openapitools.client.model.EmployeeDto getEmployee(@PathVariable("id") UUID id) {
        return employeeControl.getEmployee(id).toDto();
    }
}
