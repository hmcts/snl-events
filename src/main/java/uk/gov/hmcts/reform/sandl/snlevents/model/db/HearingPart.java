package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Audited
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "is_deleted=false")
@SuppressWarnings("squid:S3437")
public class HearingPart extends VersionedEntity implements Serializable, HistoryAuditable {

    @Id
    private UUID id;

    private String caseNumber;

    private String caseTitle;

    private String caseType;

    private String hearingType;

    private Duration duration;

    private OffsetDateTime scheduleStart;

    private OffsetDateTime scheduleEnd;

    @ManyToOne
    @JsonIgnore
    @Audited(targetAuditMode = NOT_AUDITED)
    private Session session;

    @Column(name = "session_id", updatable = false, insertable = false)
    @JsonProperty("session")
    private UUID sessionId;

    private UUID reservedJudgeId;

    private String communicationFacilitator;

    private OffsetDateTime start;

    @Enumerated(EnumType.ORDINAL)
    private Priority priority;

    private boolean isDeleted;

    @CreatedDate
    private OffsetDateTime createdAt;

    @LastModifiedDate
    private OffsetDateTime modifiedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;
}
