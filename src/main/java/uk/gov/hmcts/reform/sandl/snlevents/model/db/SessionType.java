package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import javax.persistence.OneToMany;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = "sessions")
public class SessionType implements Serializable {

    public SessionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Id
    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String description;

    @JsonIgnore
    @Getter
    @OneToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
        }, mappedBy = "sessionType")
    private Set<Session> sessions = new HashSet<>();

    @Getter
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    })
    @JoinTable(
        name = "case_type_session_type",
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
        session.setSessionType(this);
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
