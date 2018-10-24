package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.util.UUID;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserTransactionData.class)
public abstract class UserTransactionData_ {

	public static volatile SingularAttribute<UserTransactionData, Integer> counterActionOrder;
	public static volatile SingularAttribute<UserTransactionData, UserTransaction> userTransaction;
	public static volatile SingularAttribute<UserTransactionData, String> counterAction;
	public static volatile SingularAttribute<UserTransactionData, String> beforeData;
	public static volatile SingularAttribute<UserTransactionData, String> action;
	public static volatile SingularAttribute<UserTransactionData, UUID> entityId;
	public static volatile SingularAttribute<UserTransactionData, UUID> userTransactionId;
	public static volatile SingularAttribute<UserTransactionData, UUID> id;
	public static volatile SingularAttribute<UserTransactionData, String> entity;

}

