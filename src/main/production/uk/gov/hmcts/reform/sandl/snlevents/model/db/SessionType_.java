package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SessionType.class)
public abstract class SessionType_ extends uk.gov.hmcts.reform.sandl.snlevents.model.db.BaseReferenceData_ {

	public static volatile SetAttribute<SessionType, Session> sessions;
	public static volatile SetAttribute<SessionType, HearingType> hearingTypes;
	public static volatile SetAttribute<SessionType, CaseType> caseTypes;

}

