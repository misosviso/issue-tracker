package sk.gohealth.issuetracker;

import com.google.api.services.sheets.v4.Sheets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import sk.gohealth.issuetracker.cli.CommandLineInterface;
import sk.gohealth.issuetracker.integrations.googlesheets.GoogleSheetIssueAdapter;
import sk.gohealth.issuetracker.integrations.googlesheets.GoogleSheetsRepository;
import sk.gohealth.issuetracker.service.IssueService;

import static org.mockito.Mockito.mock;

@TestConfiguration
class TestConfig {

    @Bean
    public GoogleSheetIssueAdapter googleSheetIssueAdapter() {
        return new GoogleSheetIssueAdapter();
    }

    @Bean
    public CommandLineInterface cli(IssueService issueService) {
        return mock(CommandLineInterface.class);
    }


    @Bean
    public GoogleSheetsRepository googleSheetsRepository(
            @Value("${google.sheets.credentials-file}") String credentialsFile,
            @Value("${google.sheets.application-name}") String applicationName,
            @Value("${google.sheets.spreadsheet-id}") String spreadsheetId,
            GoogleSheetIssueAdapter adapter
    ) throws Exception {
        return new GoogleSheetsRepository(credentialsFile, applicationName, spreadsheetId, adapter);
    }
}

