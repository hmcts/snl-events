package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


@Entity
@AllArgsConstructor
@NoArgsConstructor
public class HearingPart implements Serializable {

    @Id
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    private String caseNumber;

    @Getter
    @Setter
    private String caseTitle;

    @Getter
    @Setter
    private String caseType;

    @Getter
    @Setter
    private String hearingType;

    @Getter
    @Setter
    private Duration duration;

    @Getter
    @Setter
    private LocalDate scheduleStart;

    @Getter
    @Setter
    private LocalDate scheduleEnd;

    @Getter
    @Setter
    @ManyToOne
    private Session session;

    @Getter
    @Setter
    private OffsetDateTime start;
}
