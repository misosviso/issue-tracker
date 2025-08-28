package sk.gohealth.issuetracker.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sk.gohealth.issuetracker.domain.Issue;
import sk.gohealth.issuetracker.domain.Status;
import sk.gohealth.issuetracker.service.IssueService;

import java.util.Arrays;
import java.util.UUID;

@Component
public class CommandLineInterface implements CommandLineRunner {

    private final IssueService issueService;

    public CommandLineInterface(IssueService issueService) {
        this.issueService = issueService;
    }

    private UUID parseUUID(String argument) {
        try {
            return UUID.fromString(argument);
        } catch (Exception e) {
            throw new IllegalArgumentException("Expecting uuid id");
        }
    }

    private Status pasrseStatus(String argument) {
        try {
            return Status.valueOf(argument.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Expecting values: " + String.join(", ", Arrays.stream(Status.values()).map(Enum::name).toList()));
        }
    }

    @Override
    public void run(String... args) {
        if (args.length < 1) {
            System.out.println("Usage: create|update|list ...");
            return;
        }

        switch (args[0]) {
            case "create" -> {
                String description = args[1];
                UUID parentId = args.length > 2 ? parseUUID(args[2]) : null;
                Issue issue = issueService.createIssue(description, parentId);
                System.out.println("Created: " + issue.getId());
            }
            case "update" -> {
                UUID id = parseUUID(args[1]);
                Status status = pasrseStatus(args[2]);
                issueService.updateStatus(id, status);
                System.out.println("Updated " + id + " to " + status);
            }
            case "list" -> {
                Status status = pasrseStatus(args[2]);
                issueService.getIssues(status).forEach(System.out::println);
            }
            default -> System.out.println("Unknown command");
        }
    }


}

