package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Audited
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@Where(clause = "is_deleted=false")
@SuppressWarnings("squid:S3437")
public class HearingPart extends VersionedEntity implements Serializable, HistoryAuditable, Statusable {

    @Id
    private UUID id;

    @ManyToOne
    @JsonIgnore
    @Audited(targetAuditMode = NOT_AUDITED)
    private Session session;

    @Column(name = "session_id", updatable = false, insertable = false)
    private UUID sessionId;

    private boolean isDeleted;

    @CreatedDate
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    private OffsetDateTime modifiedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;

    private OffsetDateTime start;

    @ManyToOne
    @JsonIgnore
    @Audited(targetAuditMode = NOT_AUDITED)
    private Hearing hearing;

    @Column(name = "hearing_id", updatable = false, insertable = false)
    private UUID hearingId;

    @ManyToOne
    @Audited(targetAuditMode = NOT_AUDITED)
    @JoinColumn(name = "status", nullable = false)
    private StatusConfig status;

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
        this.hearingId = (hearing != null) ? hearing.getId() : null;
    }

    public void setSession(Session session) {
        this.session = session;
        this.sessionId = (session != null) ? session.getId() : null;
    }
}
