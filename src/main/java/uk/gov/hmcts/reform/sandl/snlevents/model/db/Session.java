package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@Entity
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("squid:S3437")
public class Session extends VersionedEntity implements Serializable {

    @Id
    @Getter
    @Setter
    private UUID id;

    @ManyToOne
    @Getter
    @Setter
    private Person person;

    @NotNull
    @Getter
    @Setter
    private OffsetDateTime start;

    @NotNull
    @Getter
    @Setter
    private Duration duration;

    @Getter
    @Setter
    @EqualsAndHashCode.Exclude
    @Deprecated
    private String caseType;

    @ManyToOne
    @Getter
    @Setter
    private Room room;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "session")
    @JsonIgnore
    private List<HearingPart> hearingParts;

    @Getter
    @EqualsAndHashCode.Exclude
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ManyToOne(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
        })
    private SessionType sessionType;

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
        sessionType.getSessions().add(this);
    }
}
