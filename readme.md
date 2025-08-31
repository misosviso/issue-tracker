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