package sk.gohealth.issuetracker.domain;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Issue {
    private UUID id;
    private String description;
    private UUID parentId;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Issue(UUID id, String description, UUID parentId, Status status) {
        this.id = id;
        this.description = description;
        this.parentId = parentId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
    }

    public String print() {
        return this.getId() + " " + this.getDescription() + " " + this.getStatus().name();
    }
}

