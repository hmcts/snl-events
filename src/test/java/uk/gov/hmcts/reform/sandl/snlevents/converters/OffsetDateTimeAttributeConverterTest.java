package uk.gov.hmcts.reform.sandl.snlevents.converters;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OffsetDateTimeAttributeConverterTest {

    private static final long TIME_IN_MILLISECONDS = 1531832400000L;

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2018,7,17,
        13,0,0,0, ZoneOffset.UTC);

    private OffsetDateTimeAttributeConverter converter = new OffsetDateTimeAttributeConverter();

    @Test
    public void convertToEntityAttribute_shouldConvertTimestampProperly() {
        OffsetDateTime result = converter.convertToEntityAttribute(new Timestamp(TIME_IN_MILLISECONDS));

        assertEquals(OFFSET_DATE_TIME, result);
    }

    @Test
    public void convertToDatabaseColumn() {
        Timestamp result = converter.convertToDatabaseColumn(OFFSET_DATE_TIME);

        assertEquals(new Timestamp(TIME_IN_MILLISECONDS), result);
    }

    @Test
    public void shouldGiveNulls_whenCalledWithNulls() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }
}
