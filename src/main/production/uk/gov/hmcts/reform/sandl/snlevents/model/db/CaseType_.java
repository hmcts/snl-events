package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CaseType.class)
public abstract class CaseType_ extends uk.gov.hmcts.reform.sandl.snlevents.model.db.BaseReferenceData_ {

	public static volatile SetAttribute<CaseType, HearingType> hearingTypes;
	public static volatile SetAttribute<CaseType, SessionType> sessionTypes;

}

