package nl.rubium.efteling.park.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.park.boundary.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
public class EmployeeControlTest {
    @Mock
    KafkaProducer kafkaProducer;

    EmployeeControl employeeControl;

    @BeforeEach
    public void init() {
        this.employeeControl = new EmployeeControl(kafkaProducer);
    }

    @Test
    void getAssignableSkillsFromEmployeeSkill_givenCONTROL_expectCONTROLAndHOST(){
        var result = EmployeeControl.getAssignableSkillsFromEmployeeSkill(WorkplaceSkill.CONTROL);
        assertTrue(result.stream().allMatch(skill -> skill.equals(WorkplaceSkill.CONTROL) || skill.equals(WorkplaceSkill.HOST)));
    }

    @Test
    void hireEmployee_expectEmployeeAdded(){
        assertTrue(employeeControl.getEmployees().isEmpty());

        employeeControl.hireEmployee("first", "last", WorkplaceSkill.CONTROL);

        assertEquals(1, employeeControl.getEmployees().size());
    }

    @Test
    void assignEmployeeToWorkplace_employeeExists_expectEmployeeAssigned() throws JsonProcessingException {
        var workplaceDto = new WorkplaceDto();
        var skill = WorkplaceSkill.HOST;
        employeeControl.hireEmployee("first", "last", skill);
        assertFalse(employeeControl.getEmployees().get(0).isWorking());

        assertEquals(1, employeeControl.getEmployees().size());
        employeeControl.assignEmployeeToWorkplace(workplaceDto, skill);

        assertEquals(1, employeeControl.getEmployees().size());
        assertTrue(employeeControl.getEmployees().get(0).isWorking());
        verify(kafkaProducer).sendEvent(any(), any(), any());
    }

    @Test
    void assignEmployeeToWorkplace_employeesAtWork_expectNewEmployeeAssigned() throws JsonProcessingException {
        var workplaceDto = new WorkplaceDto();
        var skill = WorkplaceSkill.HOST;
        employeeControl.hireEmployee("first", "last", skill);
        assertFalse(employeeControl.getEmployees().get(0).isWorking());

        employeeControl.getEmployees().get(0).goToWork(workplaceDto, skill);
        assertTrue(employeeControl.getEmployees().get(0).isWorking());

        assertEquals(1, employeeControl.getEmployees().size());
        employeeControl.assignEmployeeToWorkplace(workplaceDto, skill);

        assertEquals(2, employeeControl.getEmployees().size());
        assertTrue(employeeControl.getEmployees().get(0).isWorking());
        verify(kafkaProducer).sendEvent(any(), any(), any());
    }

    @Test
    void assignEmployeeToWorkplace_employeeDoesNotExists_expectNewEmployeeAssigned() throws JsonProcessingException {
        var workplaceDto = new WorkplaceDto();
        var skill = WorkplaceSkill.HOST;

        assertTrue(employeeControl.getEmployees().isEmpty());
        employeeControl.assignEmployeeToWorkplace(workplaceDto, skill);

        assertEquals(1, employeeControl.getEmployees().size());
        assertTrue(employeeControl.getEmployees().get(0).isWorking());
        verify(kafkaProducer).sendEvent(any(), any(), any());
    }

}
