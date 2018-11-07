package uk.gov.hmcts.reform.sandl.snlevents.scheduledTasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

@Component
public class RulesScheduledTasks {
    private static final Logger logger = LoggerFactory.getLogger(RulesScheduledTasks.class);

    @Autowired
    private RulesService rulesService;

    @Scheduled(fixedDelay = 30000) //30 seconds
    public void reloadRulesFactsFromDb() {
        logger.debug("Checking rules engines if any needs facts reload");
//        rulesService.search()
    }
}
