package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.StatusConfigRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@ApplicationScope
public class StatusConfigService {
    private List<StatusConfig> statusConfigs = new ArrayList<>();
    private StatusConfigRepository statusConfigRepository;

    public StatusConfigService(@Autowired StatusConfigRepository statusConfigRepository) {
        this.statusConfigRepository = statusConfigRepository;
    }

    private void fetchAllConfigs() {
        if (statusConfigs.isEmpty()) {
            statusConfigs = statusConfigRepository.findAll();
        }
    }

    public List<StatusConfig> getStatusConfigs() {
        fetchAllConfigs();
        return statusConfigs;
    }

    public StatusConfig getStatusConfig(Status statusToRetrieve) {
        fetchAllConfigs();
        return getStatusConfigs().stream().filter(entry -> entry.getStatus() == statusToRetrieve).findFirst().get();
    }
}
