package uk.gov.hmcts.reform.sandl.snlevents.validation.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.MinDuration;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.NullOrNotBlank;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Duration;

@Component
public class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, String> {

    @Override
    public void initialize(NullOrNotBlank constraintAnnotation) {
        // no-op
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value.length() == 0) {
            return false;
        }

        boolean isAllWhitespace = value.matches("^\\s*$");

        return !isAllWhitespace;
    }
}
