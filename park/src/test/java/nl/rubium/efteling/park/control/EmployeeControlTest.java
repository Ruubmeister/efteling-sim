package nl.rubium.efteling.park.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.park.boundary.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.test.annotation.DirtiesContext;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
public class EmployeeControlTest {
    @Mock KafkaProducer kafkaProducer;

    EmployeeControl employeeControl;

    @BeforeEach
    public void init() {
        this.employeeControl = new EmployeeControl(kafkaProducer);
    }

    @Test
    void getAssignableSkillsFromEmployeeSkill_givenCONTROL_expectCONTROLAndHOST() {
        var result = EmployeeControl.getAssignableSkillsFromEmployeeSkill(WorkplaceSkill.CONTROL);
        assertTrue(
                result.stream()
                        .allMatch(
                                skill ->
                                        skill.equals(WorkplaceSkill.CONTROL)
                                                || skill.equals(WorkplaceSkill.HOST)));
    }

    @Test
    void hireEmployee_expectEmployeeAddedWithCorrectSkills() {
        assertTrue(employeeControl.getEmployees().isEmpty());

        var employee = employeeControl.hireEmployee("first", "last", WorkplaceSkill.CONTROL);

        assertEquals(1, employeeControl.getEmployees().size());
        assertTrue(employee.getSkills().contains(WorkplaceSkill.CONTROL));
        assertTrue(employee.getSkills().contains(WorkplaceSkill.HOST));
    }

    @Test
    void findAvailableEmployee_whenAvailable_returnsEmployee() {
        var employee = employeeControl.hireEmployee("first", "last", WorkplaceSkill.ENGINEER);

        var found = employeeControl.findAvailableEmployee(WorkplaceSkill.ENGINEER);

        assertNotNull(found);
        assertEquals(employee.getId(), found.getId());
    }

    @Test
    void findAvailableEmployee_whenWorking_returnsNull() {
        var employee = employeeControl.hireEmployee("first", "last", WorkplaceSkill.ENGINEER);
        var workplace = new WorkplaceDto();
        employee.goToWork(workplace, WorkplaceSkill.ENGINEER);

        var found = employeeControl.findAvailableEmployee(WorkplaceSkill.ENGINEER);

        assertNull(found);
    }

    @Test
    void assignEmployeeToWorkplace_existingEmployee_expectEmployeeAssigned()
            throws JsonProcessingException {
        var workplaceDto = new WorkplaceDto();
        var skill = WorkplaceSkill.HOST;
        var employee = employeeControl.hireEmployee("first", "last", skill);
        assertFalse(employee.isWorking());

        employeeControl.assignEmployeeToWorkplace(workplaceDto, skill);

        assertTrue(employee.isWorking());
        assertEquals(workplaceDto, employee.getCurrentWorkplace());
        assertEquals(skill, employee.getCurrentSkill());
        verify(kafkaProducer).sendEvent(any(), any(), any());
    }

    @Test
    void assignEmployeeToWorkplace_noAvailableEmployee_hiresNew() throws JsonProcessingException {
        var workplaceDto = new WorkplaceDto();
        var skill = WorkplaceSkill.HOST;

        assertTrue(employeeControl.getEmployees().isEmpty());
        employeeControl.assignEmployeeToWorkplace(workplaceDto, skill);

        assertEquals(1, employeeControl.getEmployees().size());
        var employee = employeeControl.getEmployees().get(0);
        assertTrue(employee.isWorking());
        assertEquals(workplaceDto, employee.getCurrentWorkplace());
        assertEquals(skill, employee.getCurrentSkill());
        verify(kafkaProducer).sendEvent(any(), any(), any());
    }

    @Test
    void releaseEmployee_whenWorking_stopsWorkAndSendsEvent() throws JsonProcessingException {
        var workplaceDto = new WorkplaceDto();
        var skill = WorkplaceSkill.SELL;
        var employee = employeeControl.hireEmployee("first", "last", skill);
        employee.goToWork(workplaceDto, skill);

        employeeControl.releaseEmployee(employee.getId());

        assertFalse(employee.isWorking());
        assertNull(employee.getCurrentWorkplace());
        assertNull(employee.getCurrentSkill());
        verify(kafkaProducer, times(1)).sendEvent(any(), any(), any());
    }

    @Test
    void releaseEmployee_whenNotFound_doesNothing() throws JsonProcessingException {
        employeeControl.releaseEmployee(UUID.randomUUID());
        verify(kafkaProducer, never()).sendEvent(any(), any(), any());
    }
}
