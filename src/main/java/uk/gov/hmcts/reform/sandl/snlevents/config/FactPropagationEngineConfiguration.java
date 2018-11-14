package uk.gov.hmcts.reform.sandl.snlevents.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class FactPropagationEngineConfiguration {
    /**
     * Name doesn't matter, it is here just to differentiate the engines easily.
     */
    private String name;

    /**
     * Endpoint used to find out if the rules engine is loaded.
     * or is loading.
     */
    private String reloadStatusUrl;

    /**
     * Endpoint where all messages with commands will be sent.
     */
    private String msgUrl;

    /**
     * Commands that should be send to the engine via the msgUrl.
     */
    private List<String> msgTypes;
}
