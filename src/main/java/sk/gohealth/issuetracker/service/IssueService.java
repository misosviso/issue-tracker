package sk.gohealth.issuetracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.gohealth.issuetracker.domain.Issue;
import sk.gohealth.issuetracker.domain.IssueRepository;
import sk.gohealth.issuetracker.domain.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class IssueService {

    private final IssueRepository repository;

    public Issue createIssue(String description, UUID parentId) {
        Issue issue = new Issue(
                UUID.randomUUID(),
                description,
                parentId,
                Status.OPEN
        );
        return repository.save(issue);
    }

    public Issue updateStatus(UUID id, Status status) {
        Issue issue = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        issue.setStatus(status);
        issue.setUpdatedAt(LocalDateTime.now());
        return repository.save(issue);
    }

    public List<Issue> getIssues(Status status) {
        return repository.findByStatus(status);
    }
}
