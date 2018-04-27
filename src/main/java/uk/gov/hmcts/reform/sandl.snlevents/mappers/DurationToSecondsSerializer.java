package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Duration;

public class DurationToSecondsSerializer extends StdSerializer<Duration> {


    protected DurationToSecondsSerializer(Class<Duration> t) {
        super(t);
    }

    public DurationToSecondsSerializer() {
        this(null);
    }

    @Override
    public void serialize(Duration duration, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber(duration.getSeconds());
    }
}
