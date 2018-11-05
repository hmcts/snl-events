package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;

@Service
public class StatusServiceManager {

    @Autowired
    StatusConfigService statusConfigService;

    public boolean canBeListed(Hearing hearing) {
        return true; // TODO implement
    }

    public boolean canBeUnlisted(Hearing hearing) {
        return true; // TODO implement
    }


    public boolean canCountInDuration(Hearing hearing) {
        return true; // TODO implement
    }
}
