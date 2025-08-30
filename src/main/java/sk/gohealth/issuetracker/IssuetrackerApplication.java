package sk.gohealth.issuetracker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sk.gohealth.issuetracker.cli.CommandLineInterface;
import sk.gohealth.issuetracker.service.IssueService;

import java.util.Scanner;

@SpringBootApplication
public class IssuetrackerApplication implements CommandLineRunner {

    private final CommandLineInterface cli;

    public IssuetrackerApplication(CommandLineInterface cli) {
        this.cli = cli;
    }

    public static void main(String[] args) {
        SpringApplication.run(IssuetrackerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        cli.run(new Scanner(System.in));
    }
}
