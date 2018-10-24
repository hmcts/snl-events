package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Availability.class)
public abstract class Availability_ {

	public static volatile SingularAttribute<Availability, Duration> duration;
	public static volatile SingularAttribute<Availability, Person> person;
	public static volatile SingularAttribute<Availability, OffsetDateTime> start;
	public static volatile SingularAttribute<Availability, UUID> id;
	public static volatile SingularAttribute<Availability, Room> room;

}

