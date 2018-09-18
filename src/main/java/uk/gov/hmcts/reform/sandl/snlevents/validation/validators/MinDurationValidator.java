package uk.gov.hmcts.reform.sandl.snlevents.validation.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.MinDuration;

import java.time.Duration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class MinDurationValidator implements ConstraintValidator<MinDuration, Duration> {
    private int minMinutes;

    @Override
    public void initialize(MinDuration constraintAnnotation) {
        minMinutes = constraintAnnotation.minMinutes();
    }

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext context) {
        return value.getSeconds() * 60 >= minMinutes;
    }
}
