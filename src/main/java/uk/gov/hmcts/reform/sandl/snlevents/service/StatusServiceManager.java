package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Statusable;

@Service
public class StatusServiceManager {

    public boolean canBeListed(Statusable entity) {
        return entity.getStatus().isCanBeListed();
    }

    public boolean canBeUnlisted(Statusable entity) {
        return entity.getStatus().isCanBeUnlisted();
    }

    public boolean shouldBeCountInUtilization(Statusable entity) {
        return entity.getStatus().isCountInUtilization();
    }
}
