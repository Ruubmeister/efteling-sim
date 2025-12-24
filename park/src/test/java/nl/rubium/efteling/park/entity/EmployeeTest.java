package nl.rubium.efteling.park.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import org.junit.jupiter.api.Test;;

public class EmployeeTest {

    @Test
    public void construct_createEmployee_expectEmployee() {
        var employee = SFEmployee.getEmployee();
        assertEquals("First", employee.getFirstName());
        assertEquals("Last", employee.getLastName());
        assertFalse(employee.getId().toString().isBlank());
    }

    @Test
    public void isWorking_employeeHasNoCurrentWorkplace_expectFalse(){
        assertFalse(SFEmployee.getEmployee(null, WorkplaceSkill.HOST).isWorking());
    }

    @Test
    public void isWorking_employeeHasNoCurrentSkill_expectFalse(){
        var workplaceDto = new org.openapitools.client.model.WorkplaceDto();
        assertFalse(SFEmployee.getEmployee(workplaceDto, null).isWorking());
    }

    @Test
    public void isWorking_employeeHasCurrentSkillAndWorkplace_expectTrue(){
        var workplaceDto = new org.openapitools.client.model.WorkplaceDto();
        assertTrue(SFEmployee.getEmployee(workplaceDto, WorkplaceSkill.HOST).isWorking());
    }

    @Test
    public void goToWork_employeeIsAssignedToWork_expectFieldsAreSet(){
        var employee = SFEmployee.getEmployee();
        var workplaceDto = new org.openapitools.client.model.WorkplaceDto();

        employee.goToWork(workplaceDto, WorkplaceSkill.SELL);

        assertEquals(workplaceDto, employee.getCurrentWorkplace());
        assertEquals(WorkplaceSkill.SELL, employee.getCurrentSkill());
    }

    @Test
    public void stopWork_employeeHasNoWork_nothingHappens(){
        var employee = SFEmployee.getEmployee(null, null);
        employee.stopWork();
        assertNull(employee.getCurrentSkill());
        assertNull(employee.getCurrentWorkplace());
    }

    @Test
    public void stopWork_employeeHasWork_stopsWorking(){
        var workplaceDto = new org.openapitools.client.model.WorkplaceDto();
        var employee = SFEmployee.getEmployee(workplaceDto, WorkplaceSkill.SELL);
        assertEquals(WorkplaceSkill.SELL, employee.getCurrentSkill());
        assertEquals(workplaceDto, employee.getCurrentWorkplace());
        employee.stopWork();
        assertNull(employee.getCurrentSkill());
        assertNull(employee.getCurrentWorkplace());
    }
}
