package uk.gov.hmcts.reform.sandl.snlevents.model.db;

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
import javax.persistence.ManyToMany;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = "sessionTypes")
public class CaseType implements Serializable {

    public CaseType(String code, String description) {
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

    @Getter
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
        }, mappedBy = "caseTypes")
    private Set<SessionType> sessionTypes = new HashSet<>();

    @Getter
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
        }, mappedBy = "caseTypes")
    private Set<HearingType> hearingTypes = new HashSet<>();

    public void addSessionType(SessionType sessionType) {
        sessionTypes.add(sessionType);
        sessionType.getCaseTypes().add(this);
    }

    public void addHearingType(HearingType hearingType) {
        hearingTypes.add(hearingType);
        hearingType.getCaseTypes().add(this);
    }
}
