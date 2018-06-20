package uk.gov.hmcts.reform.sandl.snlevents.converters;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class OffsetDateTimeAttributeConverter implements AttributeConverter<OffsetDateTime, Timestamp> {
    @Override
    public Timestamp convertToDatabaseColumn(OffsetDateTime entityValue) {
        if( entityValue == null )
            return null;

        return Timestamp.from(Instant.from(entityValue));
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(Timestamp databaseValue) {
        if( databaseValue == null )
            return null;

        return OffsetDateTime.parse(databaseValue.toInstant().toString());
    }
}
