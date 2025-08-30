package sk.gohealth.issuetracker;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import sk.gohealth.issuetracker.domain.Issue;
import sk.gohealth.issuetracker.domain.Status;
import sk.gohealth.issuetracker.integrations.googlesheets.GoogleSheetIssueAdapter;
import sk.gohealth.issuetracker.integrations.googlesheets.GoogleSheetsRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GoogleSheetsRepositoryTest {

    public static final List<Object> HEADERS = List.of("ID", "Description", "ParentID", "Status", "CreatedAt", "UpdatedAt");
    private Sheets sheetsMock;
    private Sheets.Spreadsheets spreadsheetsMock;
    private Sheets.Spreadsheets.Values valuesMock;
    private GoogleSheetIssueAdapter adapter;
    private GoogleSheetsRepository repository;

    @BeforeEach
    void setUp() {
        sheetsMock = mock(Sheets.class);
        spreadsheetsMock = mock(Sheets.Spreadsheets.class);
        valuesMock = mock(Sheets.Spreadsheets.Values.class);
        adapter = new GoogleSheetIssueAdapter();

        // setup chaining mocks
        when(sheetsMock.spreadsheets()).thenReturn(spreadsheetsMock);
        when(spreadsheetsMock.values()).thenReturn(valuesMock);

        // now we can use the test-friendly constructor
        repository = new GoogleSheetsRepository(sheetsMock, "fakeSpreadsheetId", adapter);
    }


    @Test
    @Order(1)
    void testSaveIssue() throws Exception {
        Issue issue = new Issue(UUID.randomUUID(), "Test issue", null, Status.OPEN);

        Sheets.Spreadsheets.Values.Append appendMock = mock(Sheets.Spreadsheets.Values.Append.class);
        when(valuesMock.append(anyString(), anyString(), any())).thenReturn(appendMock);
        when(appendMock.setValueInputOption(anyString())).thenReturn(appendMock);
        when(appendMock.execute()).thenReturn(null);

        ValueRange response = new ValueRange().setValues(List.of(HEADERS));
        when(valuesMock.get(anyString(), anyString())).thenReturn(mock(Sheets.Spreadsheets.Values.Get.class));
        when(valuesMock.get(anyString(), anyString()).execute()).thenReturn(response);

        Issue saved = repository.save(issue);

        assertEquals(issue.getId(), saved.getId());
        ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
        verify(valuesMock, atLeastOnce())
                .append(eq("fakeSpreadsheetId"), eq(GoogleSheetsRepository.ISSUES_RANGE), captor.capture());

        List<ValueRange> allAppends = captor.getAllValues();
        List<Object> issueRow = allAppends.stream()
                .map(ValueRange::getValues)
                .filter(Objects::nonNull)
                .flatMap(List::stream) // flatten rows
                .filter(r -> !r.isEmpty() && r.get(0).equals(issue.getId().toString()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Issue row not appended"));

        assertEquals(issue.getId().toString(), issueRow.get(0));
    }

    @Test
    @Order(2)
    void testFindByIdFound() throws Exception {
        UUID id = UUID.randomUUID();
        List<Object> row = adapter.toStorage(new Issue(id, "desc", null, Status.OPEN));

        ValueRange response = new ValueRange().setValues(List.of(HEADERS, row));
        when(valuesMock.get(anyString(), anyString())).thenReturn(mock(Sheets.Spreadsheets.Values.Get.class));
        when(valuesMock.get(anyString(), anyString()).execute()).thenReturn(response);

        Optional<Issue> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    @Order(3)
    void testFindByStatus() throws Exception {
        Issue i1 = new Issue(UUID.randomUUID(), "A", null, Status.OPEN);
        Issue i2 = new Issue(UUID.randomUUID(), "B", null, Status.CLOSED);

        List<Object> row1 = adapter.toStorage(i1);
        List<Object> row2 = adapter.toStorage(i2);

        ValueRange response = new ValueRange().setValues(List.of(HEADERS, row1, row2));
        when(valuesMock.get(anyString(), anyString())).thenReturn(mock(Sheets.Spreadsheets.Values.Get.class));
        when(valuesMock.get(anyString(), anyString()).execute()).thenReturn(response);

        List<Issue> openIssues = repository.findByStatus(Status.OPEN);
        assertEquals(1, openIssues.size());
        assertEquals(Status.OPEN, openIssues.get(0).getStatus());
    }
}

