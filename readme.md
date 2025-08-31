# IssueTracker CLI

This project is a Spring Boot CLI application for managing issues stored in Google Sheets.

## 🐳 Run with Docker

### Build inside host (requires Maven installed)

```bash
mvn clean package -DskipTests
docker build -t issuetracker-cli .
docker run -it --rm issuetracker-cli
```

### Build inside Docker (no Maven installed locally)
```bash
docker build -t issuetracker-cli .
docker run -it --rm issuetracker-cli
```

[Google sheets link](https://docs.google.com/spreadsheets/d/17UIU87doUWvKsMwXrW3o7fJY5ja2GWwtYgUM_18T3xU/edit?gid=0#gid=0)

# DOCUMENTATION

## Overview
command-line application for managing issues, with support for:
- Creating, updating, and listing issues.
- Storing issues in Google Sheets (currently) with future extensibility for databases or Jira integrations.
- Docker containerization

## Code Architecture
- CLI Layer (CommandLineInterface)
  - handles commands
- Facade Layer (IssueService)
  - abstracts away storage implementation
- Repository Layer (GoogleSheetsRepository)
  - Handles GoogleSheets CRUD
- Domain (Issue)

## Requirements
- Java 21+
- Maven 3+
- Docker (optional for containerized execution)
- Google Sheets API credentials (JSON)

