package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Audited
@EntityListeners(AuditingEntityListener.class)
@SuppressWarnings("squid:S3437")
public class Session extends VersionedEntity implements Serializable, HistoryAuditable {

    @Id
    private UUID id;

    @ManyToOne
    @Audited(targetAuditMode = NOT_AUDITED)
    private Person person;

    @NotNull
    private OffsetDateTime start;

    @NotNull
    private Duration duration;

    @EqualsAndHashCode.Exclude
    @Deprecated
    @NotAudited
    private String caseType;

    @ManyToOne
    @Audited(targetAuditMode = NOT_AUDITED)
    private Room room;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "session")
    @JsonIgnore
    @Audited(targetAuditMode = NOT_AUDITED)
    private List<HearingPart> hearingParts;

    @EqualsAndHashCode.Exclude
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ManyToOne(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
        })
    @Audited(targetAuditMode = NOT_AUDITED)
    private SessionType sessionType;

    @CreatedDate
    @Column(updatable = false)
    @EqualsAndHashCode.Exclude
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @EqualsAndHashCode.Exclude
    private OffsetDateTime modifiedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
        sessionType.getSessions().add(this);
    }
}
