package sk.gohealth.issuetracker.domain;

public interface IssueAdapter<T> {
    T toStorage(Issue issue);
    Issue toDomain(T storageIssue);
}

