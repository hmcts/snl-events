package uk.gov.hmcts.reform.sandl.snlevents.validation.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.TimeFormat;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class TimeFormatValidator implements ConstraintValidator<TimeFormat, String> {
    private String timeFormat;

    @Override
    public void initialize(TimeFormat constraintAnnotation) {
        timeFormat = constraintAnnotation.timeFormat();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        DateTimeFormatter strictTimeFormatter = DateTimeFormatter.ofPattern(timeFormat)
            .withResolverStyle(ResolverStyle.STRICT);

        try {
            LocalTime.parse(value, strictTimeFormatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
