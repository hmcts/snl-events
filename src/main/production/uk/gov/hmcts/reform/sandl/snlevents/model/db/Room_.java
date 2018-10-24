package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.util.UUID;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Room.class)
public abstract class Room_ {

	public static volatile ListAttribute<Room, Session> sessionList;
	public static volatile SingularAttribute<Room, String> name;
	public static volatile SingularAttribute<Room, UUID> id;
	public static volatile SingularAttribute<Room, RoomType> roomType;

}

