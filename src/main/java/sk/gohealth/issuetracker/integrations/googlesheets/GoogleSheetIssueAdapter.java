package sk.gohealth.issuetracker.integrations.googlesheets;

import org.springframework.stereotype.Component;
import sk.gohealth.issuetracker.domain.Issue;
import sk.gohealth.issuetracker.domain.IssueAdapter;
import sk.gohealth.issuetracker.domain.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class GoogleSheetIssueAdapter implements IssueAdapter<List<Object>> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<Object> toStorage(Issue issue) {
        return Arrays.asList(
                issue.getId() != null ? issue.getId().toString() : "",
                issue.getDescription() != null ? issue.getDescription() : "",
                issue.getParentId() != null ? issue.getParentId().toString() : "",
                issue.getStatus() != null ? issue.getStatus().name() : "",
                issue.getCreatedAt() != null ? issue.getCreatedAt().format(FORMATTER) : "",
                issue.getUpdatedAt() != null ? issue.getUpdatedAt().format(FORMATTER) : ""
        );
    }

    @Override
    public Issue toDomain(List<Object> row) {
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("Row is null or empty");
        }

        Issue issue = new Issue();

        issue.setId(parseUuid(row.get(0), true));
        issue.setDescription(parseString(row.get(1), ""));
        issue.setParentId(parseUuid(row.get(2), false));
        issue.setStatus(parseStatus(row.get(3), Status.OPEN));
        issue.setCreatedAt(parseDateTime(row.get(4), LocalDateTime.now()));
        issue.setUpdatedAt(parseDateTime(row.size() > 5 ? row.get(5) : null, null));

        return issue;
    }

    private UUID parseUuid(Object value, boolean required) {
        if (value != null && !value.toString().isBlank()) {
            return UUID.fromString(value.toString());
        }
        if (required){
            throw new IllegalArgumentException("Issue ID cannot be null or blank");
        }
        return null;
    }

    private String parseString(Object value, String defaultValue) {
        return value != null ? value.toString() : defaultValue;
    }

    private Status parseStatus(Object value, Status defaultValue) {
        if (value != null && !value.toString().isBlank()) {
            return Status.valueOf(value.toString());
        }
        return defaultValue;
    }

    private LocalDateTime parseDateTime(Object value, LocalDateTime defaultValue) {
        if (value != null && !value.toString().isBlank()) {
            return LocalDateTime.parse(value.toString(), FORMATTER);
        }
        return defaultValue;
    }
}
