package sk.gohealth.issuetracker.domain;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    OPEN,
    IN_PROGRESS,
    CLOSED
}