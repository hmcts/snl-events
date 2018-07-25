package uk.gov.hmcts.reform.sandl.snlevents.converters;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DurationConverterTest {

    private DurationConverter durationConverter = new DurationConverter();

    @Test
    public void convertToEntityAttribute_convertsSecondsProperly() {
        Duration result = durationConverter.convertToEntityAttribute(60L);

        assertEquals(Duration.ofMinutes(1), result);
    }

    @Test
    public void convertToDatabaseColumn_convertsDurationToSecondsProperly() {
        long result = durationConverter.convertToDatabaseColumn(Duration.ofSeconds(180));

        assertEquals(180, result);
    }

    @Test
    public void shouldGiveNulls_whenCalledWithNulls() {
        assertNull(durationConverter.convertToDatabaseColumn(null));
        assertNull(durationConverter.convertToEntityAttribute(null));
    }
}
