package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "is_deleted=false")
@SuppressWarnings("squid:S3437")
public class HearingPart extends VersionedEntity implements Serializable {

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
    private OffsetDateTime scheduleStart;

    @Getter
    @Setter
    private OffsetDateTime scheduleEnd;

    @Getter
    @Setter
    @ManyToOne
    @JsonIgnore
    private Session session;

    @Column(name = "session_id", updatable = false, insertable = false)
    @Getter
    @Setter
    @JsonProperty("session")
    private UUID sessionId;

    @Getter
    @Setter
    private UUID reservedJudgeId;

    @Getter
    @Setter
    private String communicationFacilitator;

    @Getter
    @Setter
    private OffsetDateTime start;

    @Getter
    @Setter
    @CreatedDate
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @Getter
    @Setter
    @LastModifiedDate
    @Column(updatable = false)
    private OffsetDateTime updatedAt;

    @Getter
    @Setter
    @Enumerated(EnumType.ORDINAL)
    private Priority priority;

    @Getter
    @Setter
    private boolean isDeleted;
}
