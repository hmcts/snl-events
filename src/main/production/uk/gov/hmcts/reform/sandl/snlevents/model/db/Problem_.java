package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Problem.class)
public abstract class Problem_ {

	public static volatile SingularAttribute<Problem, String> severity;
	public static volatile SingularAttribute<Problem, OffsetDateTime> createdAt;
	public static volatile ListAttribute<Problem, ProblemReference> references;
	public static volatile SingularAttribute<Problem, UUID> userTransactionId;
	public static volatile SingularAttribute<Problem, String> id;
	public static volatile SingularAttribute<Problem, String> type;
	public static volatile SingularAttribute<Problem, String> message;

}

