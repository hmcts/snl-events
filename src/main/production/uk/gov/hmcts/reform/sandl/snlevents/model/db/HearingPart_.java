package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(HearingPart.class)
public abstract class HearingPart_ extends uk.gov.hmcts.reform.sandl.snlevents.model.db.VersionedEntity_ {

	public static volatile SingularAttribute<HearingPart, OffsetDateTime> createdAt;
	public static volatile SingularAttribute<HearingPart, Boolean> isDeleted;
	public static volatile SingularAttribute<HearingPart, String> createdBy;
	public static volatile SingularAttribute<HearingPart, Session> session;
	public static volatile SingularAttribute<HearingPart, OffsetDateTime> modifiedAt;
	public static volatile SingularAttribute<HearingPart, OffsetDateTime> start;
	public static volatile SingularAttribute<HearingPart, Hearing> hearing;
	public static volatile SingularAttribute<HearingPart, String> modifiedBy;
	public static volatile SingularAttribute<HearingPart, UUID> id;
	public static volatile SingularAttribute<HearingPart, UUID> sessionId;
	public static volatile SingularAttribute<HearingPart, UUID> hearingId;

}

