package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "sessions")
public class SessionType implements Serializable {

    @Id
    @Getter
    @Setter
    String code;

    @Getter
    @Setter
    String description;

    @Getter
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    })
    @JoinTable(
        name = "session_session_type",
        joinColumns = {@JoinColumn(name = "session_type_code")},
        inverseJoinColumns = {@JoinColumn(name = "session_id")}
    )
    private Set<Session> sessions = new HashSet<>();

    @Getter
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    })
    @JoinTable(
        name = "case_types_session_type",
        joinColumns = {@JoinColumn(name = "session_type_code")},
        inverseJoinColumns = {@JoinColumn(name = "case_type_code")}
    )
    private Set<CaseType> caseTypes = new HashSet<>();

    @Getter
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    })
    @JoinTable(
        name = "hearing_type_session_type",
        joinColumns = {@JoinColumn(name = "session_type_code")},
        inverseJoinColumns = {@JoinColumn(name = "hearing_type_code")}
    )
    private Set<HearingType> hearingTypes = new HashSet<>();

    public void addSession(Session session) {
        sessions.add(session);
        session.getSessionTypes().add(this);
    }

    public void addCaseType(CaseType caseType) {
        caseTypes.add(caseType);
        caseType.getSessionTypes().add(this);
    }

    public void addHearingType(HearingType hearingType) {
        hearingTypes.add(hearingType);
        hearingType.getSessionTypes().add(this);
    }
}
