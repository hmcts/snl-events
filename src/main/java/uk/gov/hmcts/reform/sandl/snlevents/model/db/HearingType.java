package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HearingType extends BaseReferenceData implements Serializable {

    @EqualsAndHashCode.Exclude
    @Getter
    @JsonIgnore
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
        }, mappedBy = "hearingTypes")
    private Set<SessionType> sessionTypes = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @Getter
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    })
    @JoinTable(
        name = "hearing_type_case_type",
        joinColumns = {@JoinColumn(name = "hearing_type_code")},
        inverseJoinColumns = {@JoinColumn(name = "case_type_code")}
    )
    private Set<CaseType> caseTypes = new HashSet<>();

    public HearingType(String code, String description) {
        super(code, description);
    }

    public void addSessionType(SessionType sessionType) {
        sessionTypes.add(sessionType);
        sessionType.getHearingTypes().add(this);
    }

    public void addCaseType(CaseType caseType) {
        caseTypes.add(caseType);
        caseType.getHearingTypes().add(this);
    }
}
