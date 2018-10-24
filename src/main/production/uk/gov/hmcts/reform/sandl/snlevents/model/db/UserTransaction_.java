package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.util.UUID;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserTransaction.class)
public abstract class UserTransaction_ {

	public static volatile SingularAttribute<UserTransaction, UserTransactionRulesProcessingStatus> rulesProcessingStatus;
	public static volatile ListAttribute<UserTransaction, UserTransactionData> userTransactionDataList;
	public static volatile SingularAttribute<UserTransaction, UUID> id;
	public static volatile SingularAttribute<UserTransaction, UserTransactionStatus> status;

}

