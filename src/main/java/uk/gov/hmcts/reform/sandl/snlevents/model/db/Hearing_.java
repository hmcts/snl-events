package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.util.UUID;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Hearing.class)
public class Hearing_ {
    private Hearing_() {
    }

    public static volatile SingularAttribute<Hearing, UUID> id;
}
