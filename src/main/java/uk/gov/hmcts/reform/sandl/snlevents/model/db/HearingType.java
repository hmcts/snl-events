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
@EqualsAndHashCode(exclude = "caseTypes")
public class HearingType implements Serializable {
    @Id
    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String description;

    @Getter
    @ManyToMany(mappedBy = "hearingTypes")
    private Set<SessionType> sessionTypes = new HashSet<>();

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

    public void addSessionType(SessionType sessionType) {
        sessionTypes.add(sessionType);
        sessionType.getHearingTypes().add(this);
    }

    public void addCaseType(CaseType caseType) {
        caseTypes.add(caseType);
        caseType.getHearingTypes().add(this);
    }
}
