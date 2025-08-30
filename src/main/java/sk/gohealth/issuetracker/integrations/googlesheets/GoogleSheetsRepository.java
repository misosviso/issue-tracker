package sk.gohealth.issuetracker.integrations.googlesheets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GoogleSheetsRepository implements IssueRepository {

    public static final String ISSUES_RANGE = "Issues!A:F";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets";

    private static final List<Object> HEADERS = List.of(
            "ID", "Description", "ParentID", "Status", "CreatedAt", "UpdatedAt"
    );

    private final Sheets sheetsService;
    private final String spreadsheetId;
    private final GoogleSheetIssueAdapter adapter;

    public GoogleSheetsRepository(Sheets sheetsService, String spreadsheetId, GoogleSheetIssueAdapter adapter) {
        this.sheetsService = sheetsService;
        this.spreadsheetId = spreadsheetId;
        this.adapter = adapter;
    }

    public GoogleSheetsRepository(
            @Value("${google.sheets.credentials-file}") String credentialsFile,
            @Value("${google.sheets.application-name}") String applicationName,
            @Value("${google.sheets.spreadsheet-id}") String spreadsheetId, GoogleSheetIssueAdapter adapter
    ) throws IOException, GeneralSecurityException {
        this.adapter = adapter;
        this.sheetsService = buildSheetsService(credentialsFile, applicationName);
        this.spreadsheetId = spreadsheetId;
    }

    private Sheets buildSheetsService(String credentialsFile, String applicationName)
            throws IOException, GeneralSecurityException {

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(credentialsFile)) {
            if (in == null) {
                throw new IOException("Google Sheets credentials file not found: " + credentialsFile);
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                    .createScoped(List.of(SHEETS_SCOPE));

            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials)
            )
                    .setApplicationName(applicationName)
                    .build();
        }
    }

    @Override
    public Issue save(Issue issue) {
        try {
            ValueRange existing = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, ISSUES_RANGE)
                    .execute();

            List<List<Object>> values = existing.getValues();
            if (values == null || values.isEmpty()) {
                ValueRange headerBody = new ValueRange().setValues(List.of(HEADERS));
                sheetsService.spreadsheets().values()
                        .append(spreadsheetId, ISSUES_RANGE, headerBody)
                        .setValueInputOption("RAW")
                        .execute();
            }

            List<Object> row = adapter.toStorage(issue);
            ValueRange appendBody = new ValueRange().setValues(Collections.singletonList(row));
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, ISSUES_RANGE, appendBody)
                    .setValueInputOption("RAW")
                    .execute();

            return issue;
        } catch (IOException e) {
            throw new GoogleSheetsIssueException("Failed to save issue to Google Sheets", e);
        }
    }

    @Override
    public Optional<Issue> findById(UUID uuid) {
        try {
            String id = uuid.toString();
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, ISSUES_RANGE)
                    .execute();

            if (response.getValues() == null || response.getValues().size() <= 1)
                return Optional.empty();

            // skip headers
            for (List<Object> row : response.getValues().subList(1, response.getValues().size())) {
                if (!row.isEmpty() && row.get(0).equals(id)) {
                    return Optional.of(adapter.toDomain(row));
                }
            }

            return Optional.empty();
        } catch (IOException e) {
            throw new GoogleSheetsIssueException("Failed to read from Google Sheets", e);
        }
    }

    @Override
    public List<Issue> findByStatus(Status status) {
        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, ISSUES_RANGE)
                    .execute();

            if (response.getValues() == null || response.getValues().size() <= 1)
                return Collections.emptyList();

            // skip headers
            return response.getValues().subList(1, response.getValues().size()).stream()
                    .map(adapter::toDomain)
                    .filter(issue -> issue.getStatus() == status)
                    .toList();
        } catch (IOException e) {
            throw new GoogleSheetsIssueException("Failed to read from Google Sheets", e);
        }
    }

}

