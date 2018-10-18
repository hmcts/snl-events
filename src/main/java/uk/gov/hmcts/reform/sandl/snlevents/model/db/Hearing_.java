package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.util.UUID;

@StaticMetamodel(Hearing.class)
public class Hearing_ {
    public static volatile SingularAttribute<Hearing, UUID> id;
}
