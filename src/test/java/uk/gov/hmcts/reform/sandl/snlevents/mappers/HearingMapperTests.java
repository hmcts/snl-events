package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingRequest;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class HearingMapperTests {
    private static final UUID ID = UUID.randomUUID();

    @Test
    public void mapToHearingPart_fromCreateHearingPartRequest_shouldSetProperties() {
        val chpr = new CreateHearingRequest();
        chpr.setId(ID);

        val hp = new HearingMapper().mapToHearingPart(chpr);

        assertThat(hp.getId()).isNotNull();
        assertThat(hp.getHearingId()).isEqualTo(ID);
    }
}
