package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("fact-propagation")
@Validated
public class SubscribersConfiguration {

    private Map<String, List<String>> subscribers;

    public Map<String, List<String>> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Map<String, List<String>> subscribers) {
        this.subscribers = subscribers;
    }
}
