package nl.rubium.efteling.park.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.rubium.efteling.common.event.entity.Event;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Employee {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public Employee(String firstName, String lastName, List<WorkplaceSkill> skills){
        this.firstName = firstName;
        this.lastName = lastName;
        this.skills = skills;
    }

    private UUID id;
    private String firstName;
    private String lastName;
    private WorkplaceDto currentWorkplace;
    private List<WorkplaceSkill> skills;
    private WorkplaceSkill currentSkill;

    public void goToWork(WorkplaceDto workplaceDto, WorkplaceSkill skill){
        currentWorkplace = workplaceDto;
        currentSkill = skill;
    }

    public void stopWork(){
        if(currentWorkplace != null){
            currentWorkplace = null;
            currentSkill = null;
        }
    }

    public boolean isWorking(){
        return currentWorkplace != null && currentSkill != null;
    }
}
