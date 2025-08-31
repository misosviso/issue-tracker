package sk.gohealth.issuetracker.integrations.googlesheets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import sk.gohealth.issuetracker.domain.Issue;
import sk.gohealth.issuetracker.domain.IssueRepository;
import sk.gohealth.issuetracker.domain.Status;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.*;

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

    @Autowired
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
            List<List<Object>> values = fetchSheetValues();
            values = ensureHeaders(values);

            int rowIndex = findRowIndexById(values, issue.getId().toString());
            List<Object> newRow = adapter.toStorage(issue);

            if (rowIndex == -1) {
                appendRow(newRow);
            } else {
                updateRow(rowIndex, newRow);
            }

            return issue;
        } catch (IOException e) {
            throw new GoogleSheetsIssueException("Failed to save issue to Google Sheets", e);
        }
    }

    private List<List<Object>> fetchSheetValues() throws IOException {
        ValueRange existing = sheetsService.spreadsheets().values()
                .get(spreadsheetId, ISSUES_RANGE)
                .execute();
        return existing.getValues();
    }

    private List<List<Object>> ensureHeaders(List<List<Object>> values) throws IOException {
        if (values == null || values.isEmpty()) {
            ValueRange headerBody = new ValueRange().setValues(List.of(HEADERS));
            sheetsService.spreadsheets().values()
                    .update(spreadsheetId, ISSUES_RANGE, headerBody)
                    .setValueInputOption("RAW")
                    .execute();

            values = new ArrayList<>();
            values.add(HEADERS);
        }
        return values;
    }

    private int findRowIndexById(List<List<Object>> values, String issueId) {
        for (int i = 1; i < values.size(); i++) { // skip header row
            List<Object> row = values.get(i);
            if (!row.isEmpty() && row.get(0).equals(issueId)) {
                return i;
            }
        }
        return -1;
    }

    private void appendRow(List<Object> row) throws IOException {
        ValueRange appendBody = new ValueRange().setValues(Collections.singletonList(row));
        sheetsService.spreadsheets().values()
                .append(spreadsheetId, ISSUES_RANGE, appendBody)
                .setValueInputOption("RAW")
                .execute();
    }

    private void updateRow(int rowIndex, List<Object> row) throws IOException {
        String range = String.format("Issues!A%d:F%d", rowIndex + 1, rowIndex + 1);
        ValueRange updateBody = new ValueRange().setValues(Collections.singletonList(row));
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, updateBody)
                .setValueInputOption("RAW")
                .execute();
    }

    @Override
    public Optional<Issue> findById(UUID uuid) {
        try {
            String id = uuid.toString();
            List<List<Object>> values = fetchSheetValues();

            if (values == null || values.size() <= 1)
                return Optional.empty();

            // skip headers
            for (List<Object> row : values.subList(1, values.size())) {
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
            List<List<Object>> values = fetchSheetValues();

            if (values == null || values.size() <= 1)
                return Collections.emptyList();

            return values.subList(1, values.size()).stream()
                    .map(adapter::toDomain)
                    .filter(issue -> issue.getStatus() == status)
                    .toList();
        } catch (IOException e) {
            throw new GoogleSheetsIssueException("Failed to read from Google Sheets", e);
        }
    }

}

