package nl.rubium.efteling.park.control;

import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.Event;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.park.boundary.KafkaProducer;
import nl.rubium.efteling.park.entity.Employee;
import org.apache.commons.lang3.NotImplementedException;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class EmployeeControl {
    private CopyOnWriteArrayList<Employee> employees = new CopyOnWriteArrayList<>();

    private KafkaProducer kafkaProducer;

    public EmployeeControl(KafkaProducer kafkaProducer){
        this.kafkaProducer = kafkaProducer;
    }

    public static List<WorkplaceSkill> getAssignableSkillsFromEmployeeSkill(WorkplaceSkill skill){
        return switch (skill) {
            case CONTROL ->  List.of(WorkplaceSkill.CONTROL, WorkplaceSkill.HOST);
            case COOK -> List.of(WorkplaceSkill.COOK);
            case ENGINEER -> List.of(WorkplaceSkill.ENGINEER);
            case HOST -> List.of(WorkplaceSkill.HOST, WorkplaceSkill.SELL);
            case SELL -> List.of(WorkplaceSkill.SELL, WorkplaceSkill.HOST);
        };
    }

    public Employee hireEmployee(String firstName, String lastName, WorkplaceSkill skill){
        var skills = getAssignableSkillsFromEmployeeSkill(skill);
        var employee = new Employee(firstName, lastName, skills);

        employees.add(employee);

        log.debug("Hired employee: {} {}", employee.getFirstName(), employee.getLastName());

        return employee;
    }

    public void assignEmployeeToWorkplace(WorkplaceDto workplaceDto, WorkplaceSkill workplaceSkill){

        // Todo: Set up name service

        var employee = employees.stream()
                .filter(existingEmployee -> !existingEmployee.isWorking())
                .findFirst()
                .orElse(hireEmployee("firstName", "lastName", workplaceSkill));

        employee.goToWork(workplaceDto, workplaceSkill);

        var payload = new HashMap<String, Object>();
        payload.put("employee", employee.getId().toString());
        payload.put("workplace", workplaceDto);
        payload.put("skill", workplaceSkill);

        kafkaProducer.sendEvent(EventSource.EMPLOYEE, EventType.EMPLOYEECHANGEDWORKPLACE, payload);

        log.info("Employee {} {} assigned to workplace", employee.getFirstName(), employee.getLastName());
    }
}
