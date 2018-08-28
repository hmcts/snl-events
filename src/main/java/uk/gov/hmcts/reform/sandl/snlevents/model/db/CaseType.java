package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CaseType extends BaseReferenceData implements Serializable {

    @EqualsAndHashCode.Exclude
    @Getter
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
        }, mappedBy = "caseTypes")
    private Set<SessionType> sessionTypes = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @Getter
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
        }, mappedBy = "caseTypes")
    private Set<HearingType> hearingTypes = new HashSet<>();

    public CaseType(String code, String description) {
        super(code, description);
    }

    public void addSessionType(SessionType sessionType) {
        sessionTypes.add(sessionType);
        sessionType.getCaseTypes().add(this);
    }

    public void addHearingType(HearingType hearingType) {
        hearingTypes.add(hearingType);
        hearingType.getCaseTypes().add(this);
    }
}

