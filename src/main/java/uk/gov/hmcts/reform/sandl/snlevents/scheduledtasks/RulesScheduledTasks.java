package uk.gov.hmcts.reform.sandl.snlevents.scheduledtasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import uk.gov.hmcts.reform.sandl.snlevents.service.ReloadRulesService;

import java.io.IOException;
import java.net.ConnectException;
import javax.transaction.Transactional;

@Component
@Transactional
@EnableScheduling
@ConditionalOnProperty(
    prefix = "scheduler",
    name = "enabled", havingValue = "true")
public class RulesScheduledTasks {
    private static final Logger logger = LoggerFactory.getLogger(RulesScheduledTasks.class);

    @Autowired
    private ReloadRulesService reloadRulesService;

    @Scheduled(fixedDelay = 30000) //30 seconds
    public void reloadRulesFactsFromDb() throws IOException {
        logger.debug("Scheduled task - reloadRulesFactsFromDb - rules reload if needed");
        try {
            reloadRulesService.reloadIfNeeded();
        } catch (ResourceAccessException ex) {
            if (ex.getCause() instanceof ConnectException) {
                logger.error("Cannot connect to rules engine - scheduled task reloadRulesFactsFromDb");
                logger.debug("Cannot connect to rules engine", ex);
            } else {
                throw ex;
            }
        }
    }

    @Scheduled(fixedDelay = 30000) //30 seconds
    public void setRulesDateAndTime() throws IOException {
        logger.debug("Scheduled task - setRulesDateAndTime - set date and time elements if needed");
        try {
            reloadRulesService.reloadDateAndTimeIfNeeded();
        } catch (ResourceAccessException ex) {
            if (ex.getCause() instanceof ConnectException) {
                logger.error("Cannot connect to rules engine - scheduled task setRulesDateAndTime");
                logger.debug("Cannot connect to rules engine", ex);
            } else {
                throw ex;
            }
        }
    }
}
