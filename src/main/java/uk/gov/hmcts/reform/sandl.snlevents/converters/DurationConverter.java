package uk.gov.hmcts.reform.sandl.snlevents.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Long> {

    Logger log = Logger.getLogger(DurationConverter.class.getSimpleName());

    @Override
    public Long convertToDatabaseColumn(Duration attribute) {
        return attribute.getSeconds();
    }

    @Override
    public Duration convertToEntityAttribute(Long duration) {
        return Duration.of(duration, ChronoUnit.SECONDS);
    }
}
