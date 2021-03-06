package uk.gov.hmcts.reform.sandl.snlevents.validation.annotations;

import uk.gov.hmcts.reform.sandl.snlevents.validation.validators.MinDurationValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinDurationValidator.class)
public @interface MinDuration {
    String message() default "{uk.gov.hmcts.reform.sandl.snlevents.validation.validators.MinDuration.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int minMinutes();
}
