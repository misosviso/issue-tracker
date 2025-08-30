package sk.gohealth.issuetracker;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sk.gohealth.issuetracker.cli.CommandLineInterface;
import sk.gohealth.issuetracker.domain.Issue;
import sk.gohealth.issuetracker.domain.Status;
import sk.gohealth.issuetracker.integrations.googlesheets.GoogleSheetsRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class GoogleSheetsRepositoryFunctionalTest {

    @Autowired
    private CommandLineInterface commandLineInterface;

    @Autowired
    private GoogleSheetsRepository repository;

    @Test
    void testSaveAndRetrieve() {
        Issue issue = new Issue(UUID.randomUUID(), "Functional Test", null, Status.OPEN);
        repository.save(issue);

        Optional<Issue> found = repository.findById(issue.getId());
        assertTrue(found.isPresent());
        assertEquals(issue.getDescription(), found.get().getDescription());
    }

    @Test
    void testFindByStatus() {
        Issue openIssue = new Issue(UUID.randomUUID(), "Open Issue", null, Status.OPEN);
        Issue closedIssue = new Issue(UUID.randomUUID(), "Closed Issue", null, Status.CLOSED);

        repository.save(openIssue);
        repository.save(closedIssue);

        List<Issue> openIssues = repository.findByStatus(Status.OPEN);
        assertTrue(openIssues.stream().anyMatch(i -> i.getId().equals(openIssue.getId())));
    }
}

