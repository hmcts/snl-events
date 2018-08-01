package uk.gov.hmcts.reform.sandl.snlevents;

import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.builders.HearingPartBuilder;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.builders.SessionBuilder;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.builders.UpsertSessionBuilder;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

    @Bean
    public SessionBuilder sessionBuilder() {
        return new SessionBuilder();
    }

    @Bean
    public UpsertSessionBuilder upsertSessionBuilder() {
        return new UpsertSessionBuilder();
    }

    @Bean
    public HearingPartBuilder hearingPartBuilder() {
        return new HearingPartBuilder();
    }
}
