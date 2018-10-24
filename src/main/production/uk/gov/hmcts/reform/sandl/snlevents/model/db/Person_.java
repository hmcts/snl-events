package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.util.UUID;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Person.class)
public abstract class Person_ {

	public static volatile ListAttribute<Person, Session> sessionList;
	public static volatile SingularAttribute<Person, String> name;
	public static volatile SingularAttribute<Person, UUID> id;
	public static volatile SingularAttribute<Person, String> personType;
	public static volatile ListAttribute<Person, Availability> availabilityList;
	public static volatile SingularAttribute<Person, String> username;

}

