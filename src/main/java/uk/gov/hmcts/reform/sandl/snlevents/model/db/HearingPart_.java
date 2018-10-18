package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.util.UUID;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(HearingPart.class)
public class HearingPart_ {
    public static volatile SingularAttribute<HearingPart, UUID> id;
    public static volatile SingularAttribute<HearingPart, UUID> hearingId;
    public static volatile SingularAttribute<HearingPart, UUID> sessionId;

    private HearingPart_() {
    }
}
