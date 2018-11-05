package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.StatusConfigRepository;

import java.util.List;

@Service
@ApplicationScope
public class StatusConfigService {

    @Autowired
    private StatusConfigRepository statusConfigRepository;

    private List<StatusConfig> availableStatuses;

    private void loadStatuses() {
        if (availableStatuses == null) {
            availableStatuses = statusConfigRepository.findAll();
        }
    }

    public List<StatusConfig> getStatuses() {
        this.loadStatuses();
        return availableStatuses;
    }

    // prone to NoSuchElementException
    public StatusConfig get(Status statusToFind) {
        return this.getStatuses().stream()
            .filter(entry -> entry.getStatus().equals(statusToFind))
            .findFirst()
            .get();
    }
}
