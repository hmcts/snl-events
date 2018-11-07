package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.StatusConfigRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@ApplicationScope
public class StatusConfigService {
    private List<StatusConfig> statusConfigs = new ArrayList<>();

    @Autowired
    private StatusConfigRepository statusConfigRepository;

    private void fetchAllConfigs() {
        statusConfigs = statusConfigRepository.findAll();
    }

    public List<StatusConfig> getStatusConfigs() {
        if (statusConfigs.isEmpty()) {
            fetchAllConfigs();
        }
        return statusConfigs;
    }

}
