package sk.gohealth.issuetracker.integration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import sk.gohealth.issuetracker.domain.Issue;
import sk.gohealth.issuetracker.domain.IssueRepository;
import sk.gohealth.issuetracker.domain.Status;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class GoogleSheetsRepository implements IssueRepository {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final Sheets sheetsService;
    private final String spreadsheetId;

    public GoogleSheetsRepository(
            @Value("${google.sheets.credentials-file}") String credentialsFile,
            @Value("${google.sheets.application-name}") String applicationName,
            @Value("${google.sheets.spreadsheet-id}") String spreadsheetId) throws IOException, GeneralSecurityException {
        this.sheetsService = getSheetsService(credentialsFile, applicationName);
        this.spreadsheetId = spreadsheetId;
    }

    private Sheets getSheetsService(String credentialsFile, String applicationName)
            throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsFile))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY, requestInitializer)
                .setApplicationName(applicationName)
                .build();
    }

    @Override
    public Issue save(Issue issue) {
        try {
            List<Object> row = Arrays.asList(
                    issue.getId(),
                    issue.getDescription(),
                    issue.getParentId(),
                    issue.getStatus().name(),
                    issue.getCreatedAt().toString(),
                    issue.getUpdatedAt() != null ? issue.getUpdatedAt().toString() : ""
            );

            ValueRange appendBody = new ValueRange().setValues(Collections.singletonList(row));
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, "Issues!A:F", appendBody)
                    .setValueInputOption("RAW")
                    .execute();

            return issue;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save issue to Google Sheets", e);
        }
    }

    @Override
    public Optional<Issue> findById(UUID id) {
        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, "Issues!A:F")
                    .execute();

            if (response.getValues() == null) return Optional.empty();

            for (List<Object> row : response.getValues()) {
                if (!row.isEmpty() && row.get(0).equals(id)) {
                    return Optional.of(mapRowToIssue(row));
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from Google Sheets", e);
        }
    }

    @Override
    public List<Issue> findByStatus(Status status) {
        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, "Issues!A:F")
                    .execute();

            if (response.getValues() == null) return Collections.emptyList();

            return response.getValues().stream()
                    .skip(1) // skip header row
                    .map(this::mapRowToIssue)
                    .filter(issue -> issue.getStatus() == status)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from Google Sheets", e);
        }
    }

    private Issue mapRowToIssue(List<Object> row) {
        return Issue.builder()
                .id(UUID.fromString(row.get(0).toString()))
                .description(row.get(1).toString())
                .parentId(row.size() > 2 ? UUID.fromString(row.get(2).toString()) : null)
                .status(Status.valueOf(row.get(3).toString()))
                .createdAt(LocalDateTime.parse(row.get(4).toString()))
                .updatedAt(row.size() > 5 && !row.get(5).toString().isBlank()
                        ? LocalDateTime.parse(row.get(5).toString())
                        : null)
                .build();
    }
}

