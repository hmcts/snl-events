package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Session.class)
public abstract class Session_ extends uk.gov.hmcts.reform.sandl.snlevents.model.db.VersionedEntity_ {

	public static volatile SingularAttribute<Session, Duration> duration;
	public static volatile SingularAttribute<Session, OffsetDateTime> createdAt;
	public static volatile SingularAttribute<Session, String> createdBy;
	public static volatile SingularAttribute<Session, Person> person;
	public static volatile SingularAttribute<Session, OffsetDateTime> modifiedAt;
	public static volatile SingularAttribute<Session, OffsetDateTime> start;
	public static volatile ListAttribute<Session, HearingPart> hearingParts;
	public static volatile SingularAttribute<Session, SessionType> sessionType;
	public static volatile SingularAttribute<Session, String> modifiedBy;
	public static volatile SingularAttribute<Session, UUID> id;
	public static volatile SingularAttribute<Session, Room> room;

}

