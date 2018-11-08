package uk.gov.hmcts.reform.sandl.snlevents.scheduledTasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.service.ReloadRulesService;

import java.io.IOException;
import javax.transaction.Transactional;

@Component
@Transactional
public class RulesScheduledTasks {
    private static final Logger logger = LoggerFactory.getLogger(RulesScheduledTasks.class);

    @Autowired
    private ReloadRulesService reloadRulesService;

    @Scheduled(fixedDelay = 30000) //30 seconds
    public void reloadRulesFactsFromDb() throws IOException {
        logger.debug("Scheduled task - rules reload if needed");
        reloadRulesService.reloadIfNeeded();
    }

    @Scheduled(fixedDelay = 30000) //30 seconds
    public void setDateAndTime() throws IOException {
        logger.debug("Scheduled task - set date and time elements if needed");
        reloadRulesService.reloadDateAndTimeIfNeeded();
    }
}
