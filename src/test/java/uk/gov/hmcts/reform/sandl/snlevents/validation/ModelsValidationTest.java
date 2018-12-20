package uk.gov.hmcts.reform.sandl.snlevents.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AmendSessionRequest;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class ModelsValidationTest {

    private static Validator validator =  Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void validateAmendSessionRequest_shouldGiveNoErrors() {
        AmendSessionRequest amendSessionRequest = new AmendSessionRequest();
        amendSessionRequest.setId(UUID.randomUUID());
        amendSessionRequest.setUserTransactionId(UUID.randomUUID());
        amendSessionRequest.setDurationInSeconds(Duration.ofMinutes(2));
        amendSessionRequest.setStartTime(OffsetDateTime.now());
        amendSessionRequest.setSessionTypeCode("f-track");
        amendSessionRequest.setVersion(0L);

        assertThat(validator.validate(amendSessionRequest).isEmpty(), is(true));
    }

    @Test
    public void validateAmendSessionRequest_shouldGiveAllErrors() {
        AmendSessionRequest amendSessionRequest = new AmendSessionRequest();
        amendSessionRequest.setDurationInSeconds(Duration.ofMinutes(0));

        assertThat(validator.validate(amendSessionRequest).size(), is(6));
    }

    @Test
    public void validateAmendSessionRequest_shouldGiveError() {
        AmendSessionRequest amendSessionRequest = new AmendSessionRequest();
        amendSessionRequest.setId(UUID.randomUUID());
        amendSessionRequest.setUserTransactionId(UUID.randomUUID());
        amendSessionRequest.setDurationInSeconds(Duration.ofMinutes(0));
        amendSessionRequest.setStartTime(OffsetDateTime.now());
        amendSessionRequest.setSessionTypeCode("f-track");
        amendSessionRequest.setVersion(0L);

        assertThat(validator.validate(amendSessionRequest).size(), is(1));
    }
}
