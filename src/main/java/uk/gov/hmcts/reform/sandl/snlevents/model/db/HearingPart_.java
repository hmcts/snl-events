package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.util.UUID;

@StaticMetamodel(HearingPart.class)
public class HearingPart_ {
    public static volatile SingularAttribute<HearingPart, UUID> id;
    public static volatile SingularAttribute<HearingPart, UUID> hearingId;
}
