package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionAmendResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionSearchResponse;

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
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
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

@SqlResultSetMappings({ //NOSONAR
    @SqlResultSetMapping(name = "MapToSessionSearchResponse",
    classes = {
        @ConstructorResult(
            targetClass = SessionSearchResponse.class,
            columns = {
                @ColumnResult(name = "session_id", type = UUID.class),
                @ColumnResult(name = "person_id", type = UUID.class),
                @ColumnResult(name = "person_name", type = String.class),
                @ColumnResult(name = "room_id", type = UUID.class),
                @ColumnResult(name = "room_name", type = String.class),
                @ColumnResult(name = "session_type_code", type = String.class),
                @ColumnResult(name = "session_type_description", type = String.class),
                @ColumnResult(name = "session_startTime", type = OffsetDateTime.class),
                @ColumnResult(name = "session_startDate", type = OffsetDateTime.class),
                @ColumnResult(name = "session_duration", type = Duration.class),
                @ColumnResult(name = "no_of_hearing_parts", type = int.class),
                @ColumnResult(name = "session_version", type = Long.class),
                @ColumnResult(name = "allocated_duration", type = Duration.class),
                @ColumnResult(name = "utilisation", type = Long.class),
                @ColumnResult(name = "available", type = Duration.class),
            })
    }),
    @SqlResultSetMapping(name = "MapToSessionAmendResponse",
        classes = {
            @ConstructorResult(
                targetClass = SessionAmendResponse.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "start", type = OffsetDateTime.class),
                    @ColumnResult(name = "duration", type = Duration.class),
                    @ColumnResult(name = "session_type_code", type = String.class),
                    @ColumnResult(name = "person_name", type = String.class),
                    @ColumnResult(name = "room_name", type = String.class),
                    @ColumnResult(name = "room_description", type = String.class),
                    @ColumnResult(name = "room_type_code", type = String.class),
                    @ColumnResult(name = "hearing_parts_count", type = Integer.class),
                    @ColumnResult(name = "has_multi_session_hearing_assigned", type = Boolean.class),
                    @ColumnResult(name = "version", type = Long.class),
                    @ColumnResult(name = "has_listed_hearing_parts", type = Boolean.class),
                })
        })
})
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

    @ManyToOne
    @Audited(targetAuditMode = NOT_AUDITED)
    private Room room;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "session")
    @JsonIgnore
    @NotAudited
    private List<HearingPart> hearingParts = new ArrayList<>();

    @NotNull
    @EqualsAndHashCode.Exclude
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

    public void addHearingPart(HearingPart hearingPart) {
        hearingPart.setSession(this);
        hearingParts.add(hearingPart);
    }
}
