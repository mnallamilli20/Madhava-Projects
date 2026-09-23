package tables.course;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Academic categories for courses")
public enum Subject {
    COMPUTER_SCIENCE,
    MATHEMATICS,
    PHYSICS,
    BIOLOGY,
    CHEMISTRY,
    ENGLISH,
    HISTORY,
    ECONOMICS,
    BUSINESS,
    COMPUTER_ENGINEERING,
    CYBERSECURITY_ENGINEERING,
}