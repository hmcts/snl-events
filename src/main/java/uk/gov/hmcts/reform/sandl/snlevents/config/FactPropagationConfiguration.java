package uk.gov.hmcts.reform.sandl.snlevents.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("fact-propagation")
@Validated
@Getter
@Setter
public class FactPropagationConfiguration {
    /**
     * All engines that will be connected.
     */
    private List<FactPropagationEngineConfiguration> engines;

    public List<String> getMsgUrlsForMsgType(String msgType) {
        return getEngines().stream()
            .filter(e -> e.getMsgTypes().stream()
                .filter(type -> type.equalsIgnoreCase(msgType))
                .findAny().isPresent())
            .map(m -> m.getMsgUrl())
            .collect(Collectors.toList());

    }
}
