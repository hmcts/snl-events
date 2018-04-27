package uk.gov.hmcts.reform.sandl.snlevents.converters;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Long> {
    @Override
    public Long convertToDatabaseColumn(Duration attribute) {
        return attribute.getSeconds();
    }

    @Override
    public Duration convertToEntityAttribute(Long duration) {
        return Duration.of(duration, ChronoUnit.SECONDS);
    }
}
