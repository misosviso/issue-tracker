package sk.gohealth.issuetracker.integrations.googlesheets;

public class GoogleSheetsIssueException extends RuntimeException {
    public GoogleSheetsIssueException(String message) {
        super(message);
    }

    public GoogleSheetsIssueException(String message, Throwable cause) {
        super(message, cause);
    }
}
