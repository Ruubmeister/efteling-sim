package nl.rubium.efteling.common.location.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkplaceTest {
    private Workplace workplace;

    @BeforeEach
    void setUp() {
        workplace = new Workplace(LocationType.RIDE);
    }

    @Test
    void setRequiredSkillCount_newSkill_shouldInitializeCurrentCount() {
        workplace.setRequiredSkillCount(WorkplaceSkill.CONTROL, 2);

        assertEquals(2, workplace.getRequiredSkillCount().get(WorkplaceSkill.CONTROL));
        assertEquals(0, workplace.getCurrentSkillCount().get(WorkplaceSkill.CONTROL));
    }

    @Test
    void needsEmployee_whenBelowRequired_shouldReturnTrue() {
        workplace.setRequiredSkillCount(WorkplaceSkill.ENGINEER, 2);
        workplace.addEmployee(WorkplaceSkill.ENGINEER);

        assertTrue(workplace.needsEmployee(WorkplaceSkill.ENGINEER));
    }

    @Test
    void needsEmployee_whenAtRequired_shouldReturnFalse() {
        workplace.setRequiredSkillCount(WorkplaceSkill.ENGINEER, 1);
        workplace.addEmployee(WorkplaceSkill.ENGINEER);

        assertFalse(workplace.needsEmployee(WorkplaceSkill.ENGINEER));
    }

    @Test
    void addEmployee_shouldIncrementCurrentCount() {
        workplace.setRequiredSkillCount(WorkplaceSkill.SELL, 2);
        workplace.addEmployee(WorkplaceSkill.SELL);
        workplace.addEmployee(WorkplaceSkill.SELL);

        assertEquals(2, workplace.getCurrentSkillCount().get(WorkplaceSkill.SELL));
    }

    @Test
    void removeEmployee_shouldDecrementCurrentCount() {
        workplace.setRequiredSkillCount(WorkplaceSkill.HOST, 2);
        workplace.addEmployee(WorkplaceSkill.HOST);
        workplace.addEmployee(WorkplaceSkill.HOST);
        workplace.removeEmployee(WorkplaceSkill.HOST);

        assertEquals(1, workplace.getCurrentSkillCount().get(WorkplaceSkill.HOST));
    }

    @Test
    void removeEmployee_whenZero_shouldStayAtZero() {
        workplace.setRequiredSkillCount(WorkplaceSkill.COOK, 1);
        workplace.removeEmployee(WorkplaceSkill.COOK);

        assertEquals(0, workplace.getCurrentSkillCount().get(WorkplaceSkill.COOK));
    }

    @Test
    void getMissingSkillCounts_whenMissingEmployees_shouldReturnDifference() {
        workplace.setRequiredSkillCount(WorkplaceSkill.CONTROL, 3);
        workplace.addEmployee(WorkplaceSkill.CONTROL);

        var missing = workplace.getMissingSkillCounts();
        assertEquals(2, missing.get(WorkplaceSkill.CONTROL));
    }

    @Test
    void getMissingSkillCounts_whenNotMissing_shouldNotIncludeSkill() {
        workplace.setRequiredSkillCount(WorkplaceSkill.SELL, 1);
        workplace.addEmployee(WorkplaceSkill.SELL);

        var missing = workplace.getMissingSkillCounts();
        assertFalse(missing.containsKey(WorkplaceSkill.SELL));
    }
}
