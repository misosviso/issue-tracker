package sk.gohealth.issuetracker.cli;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sk.gohealth.issuetracker.domain.Issue;
import sk.gohealth.issuetracker.domain.Status;
import sk.gohealth.issuetracker.service.IssueService;

import java.util.Scanner;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommandLineInterface {

    private final IssueService issueService;

    public void run(Scanner scanner) {
        System.out.println("Issuetracker CLI (type 'help' for commands)");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 3);
            String cmd = parts[0].toLowerCase();

            try {
                switch (cmd) {
                    case "create" -> handleCreate(parts);
                    case "update" -> handleUpdate(parts);
                    case "list" -> handleList(parts);
                    case "help" -> printHelp();
                    case "exit" -> {
                        System.out.println("Bye!");
                        return;
                    }
                    default -> System.out.println("Unknown command: " + cmd);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleCreate(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: create <description> [parentId]");
            return;
        }
        String description = parts[1];
        UUID parent = null;
        if (parts.length == 3 && !parts[2].isEmpty()) {
            try {
                parent = UUID.fromString(parts[2]);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid parentId format: " + parts[2]);
                return;
            }
        }
        UUID id = issueService.createIssue(description, parent).getId();
        System.out.println("Created issue: " + id);
    }

    private void handleUpdate(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: update <issueId> <status>");
            return;
        }
        UUID id;
        try {
            id = UUID.fromString(parts[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid issueId format: " + parts[1]);
            return;
        }

        Status status;
        try {
            status = Status.valueOf(parts[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status: " + parts[2]);
            return;
        }

        issueService.updateStatus(id, status);
        System.out.println("Updated " + id + " to " + status);
    }

    private void handleList(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: list <status>");
            return;
        }
        Status status;
        try {
            status = Status.valueOf(parts[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status: " + parts[1]);
            return;
        }

        var issues = issueService.getIssues(status);
        if (issues.isEmpty()) {
            System.out.println("No issues found with status: " + status);
        } else {
            issues.forEach(Issue::print);
        }
    }

    private void printHelp() {
        System.out.println("""
            Commands:
              create <description> [parentId] - Create a new issue
              update <issueId> <status>      - Update issue status
              list <status>                  - List issues by status
              help                           - Show this help
              exit                           - Exit CLI
            """);
    }
}


