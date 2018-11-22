package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingForListingResponse;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@SqlResultSetMappings({
    @SqlResultSetMapping(name = "MapToHearingForListingResponse",
        classes = {
            @ConstructorResult(
                targetClass = HearingForListingResponse.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "case_number", type = String.class),
                    @ColumnResult(name = "case_title", type = String.class),
                    @ColumnResult(name = "case_type_code", type = String.class),
                    @ColumnResult(name = "case_type_description", type = String.class),
                    @ColumnResult(name = "hearing_type_code", type = String.class),
                    @ColumnResult(name = "hearing_type_description", type = String.class),
                    @ColumnResult(name = "duration", type = Duration.class),
                    @ColumnResult(name = "schedule_start", type = OffsetDateTime.class),
                    @ColumnResult(name = "schedule_end", type = OffsetDateTime.class),
                    @ColumnResult(name = "version", type = Long.class),
                    @ColumnResult(name = "priority", type = Integer.class),
                    @ColumnResult(name = "communication_facilitator", type = String.class),
                    @ColumnResult(name = "reserved_judge_id", type = UUID.class),
                    @ColumnResult(name = "reserved_judge_name", type = String.class),
                    @ColumnResult(name = "number_of_sessions", type = Integer.class),
                    @ColumnResult(name = "status", type = String.class),
                    @ColumnResult(name = "is_multisession", type = Boolean.class),
                })
        })
})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Audited
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@Where(clause = "is_deleted=false")
@SuppressWarnings("squid:S3437")
public class Hearing extends VersionedEntity implements Serializable, HistoryAuditable, Statusable {

    @Id
    private UUID id;

    private String caseNumber;

    private String caseTitle;

    @ManyToOne
    @Audited(targetAuditMode = NOT_AUDITED)
    private CaseType caseType;

    @ManyToOne
    @Audited(targetAuditMode = NOT_AUDITED)
    private HearingType hearingType;

    private Duration duration;

    private int numberOfSessions;

    @Column(name = "is_multisession")
    private boolean isMultiSession;

    private OffsetDateTime scheduleStart;

    private OffsetDateTime scheduleEnd;

    private String communicationFacilitator;

    @Enumerated(EnumType.ORDINAL)
    private Priority priority;

    private boolean isDeleted;

    @ManyToOne
    @Audited(targetAuditMode = NOT_AUDITED)
    @JoinColumn(name = "reservedJudgeId")
    private Person reservedJudge;

    public UUID getReservedJudgeId() {
        return this.reservedJudge != null ? this.reservedJudge.getId() : null;
    }

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

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "hearing", orphanRemoval = true, cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @NotAudited
    private List<HearingPart> hearingParts = new ArrayList<>();

    public void addHearingPart(HearingPart hearingPart) {
        hearingPart.setHearing(this);
        hearingParts.add(hearingPart);
    }

    @ManyToOne
    @Audited(targetAuditMode = NOT_AUDITED)
    @JoinColumn(name = "status", nullable = false)
    private StatusConfig status;
}
