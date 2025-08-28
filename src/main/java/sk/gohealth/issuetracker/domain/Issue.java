package sk.gohealth.issuetracker.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
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
}

