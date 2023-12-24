package nl.rubium.efteling.visitors.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VisitorRepositoryTest {

    VisitorRepository visitorRepository;

    @BeforeEach
    public void setup() {
        var visitorList = new CopyOnWriteArrayList<Visitor>();
        visitorList.add(SFVisitor.getVisitor());
        visitorList.add(SFVisitor.getVisitor());
        visitorList.add(SFVisitor.getVisitor());
        visitorRepository = new VisitorRepository(visitorList);
    }

    @Test
    void all_givenVisitorsInRepo_expectVisitors() {
        assertEquals(3, visitorRepository.all().size());
    }

    @Test
    void all_givenNoVisitorsInRepo_expectEmpty() {
        var visitorRepository = new VisitorRepository();
        assertTrue(visitorRepository.all().isEmpty());
    }

    @Test
    void getVisitor_visitorExistsInRepo_expectVisitor() {
        var visitor = SFVisitor.getVisitor();
        var visitorList = new CopyOnWriteArrayList<Visitor>();
        visitorList.add(visitor);

        visitorRepository = new VisitorRepository(visitorList);

        assertEquals(visitor, visitorRepository.getVisitor(visitor.getId()));
    }

    @Test
    void getVisitor_visitorMissingInRepo_expectException() {
        var visitor = SFVisitor.getVisitor();
        visitorRepository = new VisitorRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> visitorRepository.getVisitor(visitor.getId()));
    }

    @Test
    void addVisitors_givenZeroToAdd_expectNoVisitorsAdded() {
        var currentVisitors = visitorRepository.all().size();

        visitorRepository.addVisitors(0);

        assertEquals(currentVisitors, visitorRepository.all().size());
    }

    @Test
    void addVisitors_givenThreeToAdd_expectThreeVisitorsAdded() {
        var currentVisitors = visitorRepository.all().size();

        visitorRepository.addVisitors(3);

        assertEquals(currentVisitors + 3, visitorRepository.all().size());
    }

    @Test
    void idleVisitors_givenNoIdleVisitors_expectEmptyList() {
        var futureDate = LocalDateTime.now().plusMinutes(1);
        var visitorList = new CopyOnWriteArrayList<Visitor>();

        visitorList.add(SFVisitor.getVisitor(futureDate));
        visitorList.add(SFVisitor.getVisitor(futureDate));
        visitorList.add(SFVisitor.getVisitor(futureDate));
        visitorList.add(SFVisitor.getVisitor(futureDate));
        visitorList.add(SFVisitor.getVisitor(futureDate));
        visitorRepository = new VisitorRepository(visitorList);

        assertTrue(visitorRepository.idleVisitors().isEmpty());
    }

    @Test
    void idleVisitors_givenFewIdleVisitors_expectListOfThoseVisitors() {
        var futureDate = LocalDateTime.now().plusMinutes(1);
        var visitorList = new CopyOnWriteArrayList<Visitor>();
        visitorList.add(SFVisitor.getVisitor(futureDate));
        visitorList.add(SFVisitor.getVisitor(LocalDateTime.now()));
        visitorList.add(SFVisitor.getVisitor(LocalDateTime.now().minusMinutes(5)));
        visitorList.add(SFVisitor.getVisitor(futureDate));
        visitorList.add(SFVisitor.getVisitor(futureDate));
        visitorRepository = new VisitorRepository(visitorList);

        assertEquals(2, visitorRepository.idleVisitors().size());
    }
}
