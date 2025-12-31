package nl.rubium.efteling.park.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.park.boundary.KafkaProducer;
import nl.rubium.efteling.park.entity.Employee;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.NavigationApi;
import org.openapitools.client.model.NavigationRequestDto;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class EmployeeControl {
    private CopyOnWriteArrayList<Employee> employees = new CopyOnWriteArrayList<>();
    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;
    private final NavigationApi navigationApi;

    private static final Map<WorkplaceSkill, List<WorkplaceSkill>> SKILL_ASSIGNMENTS =
            Map.of(
                    WorkplaceSkill.CONTROL, List.of(WorkplaceSkill.CONTROL, WorkplaceSkill.HOST),
                    WorkplaceSkill.COOK, List.of(WorkplaceSkill.COOK),
                    WorkplaceSkill.ENGINEER, List.of(WorkplaceSkill.ENGINEER),
                    WorkplaceSkill.HOST, List.of(WorkplaceSkill.HOST, WorkplaceSkill.SELL),
                    WorkplaceSkill.SELL, List.of(WorkplaceSkill.SELL, WorkplaceSkill.HOST));

    @Autowired
    public EmployeeControl(KafkaProducer kafkaProducer) {
        this.objectMapper = new ObjectMapper();
        this.kafkaProducer = kafkaProducer;
        this.navigationApi = new NavigationApi();
    }

    // Test constructor
    public EmployeeControl(KafkaProducer kafkaProducer, NavigationApi navigationApi) {
        this.objectMapper = new ObjectMapper();
        this.kafkaProducer = kafkaProducer;
        this.navigationApi = navigationApi;
    }

    public static List<WorkplaceSkill> getAssignableSkillsFromEmployeeSkill(WorkplaceSkill skill) {
        return SKILL_ASSIGNMENTS.getOrDefault(skill, List.of());
    }

    public Employee hireEmployee(String firstName, String lastName, WorkplaceSkill skill) {
        var skills = getAssignableSkillsFromEmployeeSkill(skill);
        var employee = new Employee(firstName, lastName, skills);
        employees.add(employee);
        log.info("Hired employee: {} {} with primary skill {}", firstName, lastName, skill);
        return employee;
    }

    public CopyOnWriteArrayList<Employee> getEmployees() {
        return employees;
    }

    public Employee findAvailableEmployee(WorkplaceSkill requiredSkill) {
        return employees.stream()
                .filter(employee -> !employee.isWorking())
                .filter(employee -> employee.getSkills().contains(requiredSkill))
                .findFirst()
                .orElse(null);
    }

    public void assignEmployeeToWorkplace(WorkplaceDto workplaceDto, WorkplaceSkill workplaceSkill)
            throws JsonProcessingException {
        log.debug(
                "assignEmployeeToWorkplace called for workplace {} with skill {}",
                workplaceDto.getId(),
                workplaceSkill);

        var employee = findAvailableEmployee(workplaceSkill);

        if (employee == null) {
            log.debug("No available employee found, hiring new employee");
            employee =
                    hireEmployee(
                            generateEmployeeName("firstName"),
                            generateEmployeeName("lastName"),
                            workplaceSkill);
        } else {
            log.debug(
                    "Found available employee: {} {}",
                    employee.getFirstName(),
                    employee.getLastName());
        }

        employee.goToWork(workplaceDto, workplaceSkill);
        log.debug(
                "Employee {} {} marked as working at workplace {}",
                employee.getFirstName(),
                employee.getLastName(),
                workplaceDto.getId());

        // Set path to workplace using navigation service
        try {
            var navigationRequest =
                    NavigationRequestDto.builder()
                            .startX(employee.getCurrentLocation().getX())
                            .startY(employee.getCurrentLocation().getY())
                            .destX(workplaceDto.getLocation().getX())
                            .destY(workplaceDto.getLocation().getY())
                            .build();

            log.debug(
                    "Requesting navigation path from ({},{}) to ({},{})",
                    employee.getCurrentLocation().getX(),
                    employee.getCurrentLocation().getY(),
                    workplaceDto.getLocation().getX(),
                    workplaceDto.getLocation().getY());

            var pathSteps = navigationApi.postNavigate(navigationRequest);
            employee.setPathToWorkplace(pathSteps);
            log.info(
                    "Employee {} {} started walking to workplace at ({},{}) with {} path steps",
                    employee.getFirstName(),
                    employee.getLastName(),
                    workplaceDto.getLocation().getX(),
                    workplaceDto.getLocation().getY(),
                    pathSteps.size());
        } catch (ApiException e) {
            log.error("Failed to get path for employee to workplace: {}", e.getMessage(), e);
            return;
        }

        var payload = new HashMap<String, String>();
        payload.put("employee", employee.getId().toString());
        payload.put("workplace", objectMapper.writeValueAsString(workplaceDto));
        payload.put("skill", workplaceSkill.name());

        kafkaProducer.sendEvent(EventSource.EMPLOYEE, EventType.EMPLOYEECHANGEDWORKPLACE, payload);
        log.info(
                "Employee {} {} (ID: {}) assigned to workplace {} with skill {}",
                employee.getFirstName(),
                employee.getLastName(),
                employee.getId(),
                workplaceDto.getId(),
                workplaceSkill);
    }

    public void releaseEmployee(UUID employeeId) throws JsonProcessingException {
        employees.stream()
                .filter(e -> e.getId().equals(employeeId))
                .findFirst()
                .ifPresent(
                        employee -> {
                            var workplace = employee.getCurrentWorkplace();
                            employee.stopWork();

                            try {
                                var payload = new HashMap<String, String>();
                                payload.put("employee", employee.getId().toString());
                                payload.put(
                                        "workplace", objectMapper.writeValueAsString(workplace));
                                kafkaProducer.sendEvent(
                                        EventSource.EMPLOYEE,
                                        EventType.EMPLOYEECHANGEDWORKPLACE,
                                        payload);
                                log.info(
                                        "Employee {} {} released from workplace",
                                        employee.getFirstName(),
                                        employee.getLastName());
                            } catch (JsonProcessingException e) {
                                log.error("Failed to send employee release event", e);
                            }
                        });
    }

    private String generateEmployeeName(String type) {
        // In a real system, this would use a proper name generation service
        return type + UUID.randomUUID().toString().substring(0, 8);
    }

    public List<Employee> getAllEmployees() {
        return List.copyOf(employees);
    }

    public Employee getEmployee(UUID id) {
        return employees.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Scheduled(fixedRate = 1000) // Update every second (faster than visitors)
    public void updateEmployeeMovement() {
        if (!employees.isEmpty()) {
            log.debug("Updating movement for {} employees", employees.size());
        }
        employees.forEach(
                employee -> {
                    if (employee.isMoving()) {
                        log.trace(
                                "Moving employee {} {} at ({},{})",
                                employee.getFirstName(),
                                employee.getLastName(),
                                employee.getCurrentLocation().getX(),
                                employee.getCurrentLocation().getY());
                    }
                    employee.moveTowardsTarget();
                });
    }
}
