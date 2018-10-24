package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Hearing.class)
public abstract class Hearing_ extends uk.gov.hmcts.reform.sandl.snlevents.model.db.VersionedEntity_ {

	public static volatile SingularAttribute<Hearing, HearingType> hearingType;
	public static volatile SingularAttribute<Hearing, Person> reservedJudge;
	public static volatile SingularAttribute<Hearing, OffsetDateTime> modifiedAt;
	public static volatile SingularAttribute<Hearing, String> caseTitle;
	public static volatile SingularAttribute<Hearing, Priority> priority;
	public static volatile SingularAttribute<Hearing, String> communicationFacilitator;
	public static volatile SingularAttribute<Hearing, CaseType> caseType;
	public static volatile SingularAttribute<Hearing, Duration> duration;
	public static volatile SingularAttribute<Hearing, OffsetDateTime> createdAt;
	public static volatile SingularAttribute<Hearing, OffsetDateTime> scheduleStart;
	public static volatile SingularAttribute<Hearing, Boolean> isDeleted;
	public static volatile SingularAttribute<Hearing, String> createdBy;
	public static volatile SingularAttribute<Hearing, String> caseNumber;
	public static volatile ListAttribute<Hearing, HearingPart> hearingParts;
	public static volatile SingularAttribute<Hearing, OffsetDateTime> scheduleEnd;
	public static volatile SingularAttribute<Hearing, String> modifiedBy;
	public static volatile SingularAttribute<Hearing, UUID> id;

}

