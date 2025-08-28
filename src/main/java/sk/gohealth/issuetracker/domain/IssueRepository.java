package sk.gohealth.issuetracker.domain;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface IssueRepository {
    Issue save(Issue issue);
    Optional<Issue> findById(UUID id);
    List<Issue> findByStatus(Status status);
}
