package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@Entity
@EqualsAndHashCode
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
    private String caseType;

    @ManyToOne
    @Getter
    @Setter
    private Room room;

    @OneToMany(mappedBy = "session")
    @JsonIgnore
    private List<HearingPart> hearingParts;

    @Getter
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
        }, mappedBy = "sessions")
    private Set<SessionType> sessionTypes = new HashSet<>();

    public void addSessionType(SessionType sessionType) {
        sessionTypes.add(sessionType);
        sessionType.getSessions().add(this);
    }
}
