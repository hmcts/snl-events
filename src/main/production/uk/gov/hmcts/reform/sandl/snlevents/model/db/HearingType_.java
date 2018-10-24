package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(HearingType.class)
public abstract class HearingType_ extends uk.gov.hmcts.reform.sandl.snlevents.model.db.BaseReferenceData_ {

	public static volatile SetAttribute<HearingType, SessionType> sessionTypes;
	public static volatile SetAttribute<HearingType, CaseType> caseTypes;

}

