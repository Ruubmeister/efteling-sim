package nl.rubium.efteling.park.control;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.park.boundary.KafkaProducer;
import nl.rubium.efteling.park.entity.Employee;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmployeeControl {
    private CopyOnWriteArrayList<Employee> employees = new CopyOnWriteArrayList<>();

    private KafkaProducer kafkaProducer;

    private final ObjectMapper objectMapper;

    @Autowired
    public EmployeeControl(KafkaProducer kafkaProducer) {
        this.objectMapper = new ObjectMapper();
        this.kafkaProducer = kafkaProducer;
    }

    public static List<WorkplaceSkill> getAssignableSkillsFromEmployeeSkill(WorkplaceSkill skill) {
        return switch (skill) {
            case CONTROL -> List.of(WorkplaceSkill.CONTROL, WorkplaceSkill.HOST);
            case COOK -> List.of(WorkplaceSkill.COOK);
            case ENGINEER -> List.of(WorkplaceSkill.ENGINEER);
            case HOST -> List.of(WorkplaceSkill.HOST, WorkplaceSkill.SELL);
            case SELL -> List.of(WorkplaceSkill.SELL, WorkplaceSkill.HOST);
        };
    }

    public Employee hireEmployee(String firstName, String lastName, WorkplaceSkill skill) {
        var skills = getAssignableSkillsFromEmployeeSkill(skill);
        var employee = new Employee(firstName, lastName, skills);

        employees.add(employee);

        log.debug("Hired employee: {} {}", employee.getFirstName(), employee.getLastName());

        return employee;
    }

    public CopyOnWriteArrayList<Employee> getEmployees(){
        return employees;
    }

    public void assignEmployeeToWorkplace(
            WorkplaceDto workplaceDto, WorkplaceSkill workplaceSkill) throws JsonProcessingException {

        // Todo: Set up name service

        var employee =
                employees.stream()
                        .filter(existingEmployee -> !existingEmployee.isWorking())
                        .findFirst()
                        .orElseGet(() -> hireEmployee("firstName", "lastName", workplaceSkill));

        employee.goToWork(workplaceDto, workplaceSkill);

        var payload = new HashMap<String, String>();
        payload.put("employee", employee.getId().toString());
        payload.put("workplace", objectMapper.writeValueAsString(workplaceDto));
        payload.put("skill", workplaceSkill.name());

        kafkaProducer.sendEvent(EventSource.EMPLOYEE, EventType.EMPLOYEECHANGEDWORKPLACE, payload);

        log.info(
                "Employee {} {} assigned to workplace",
                employee.getFirstName(),
                employee.getLastName());
    }
}
