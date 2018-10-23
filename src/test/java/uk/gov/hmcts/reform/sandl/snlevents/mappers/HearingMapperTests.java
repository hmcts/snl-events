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
    public void mapToHearingParts_fromCreateHearingPartRequestWithOneSessionToCreate_shouldSetProperties() {
        val chpr = new CreateHearingRequest();
        chpr.setId(ID);
        chpr.setNumberOfSessions(1);

        val hp = new HearingMapper().mapToHearingParts(chpr);

        assertThat(hp.size()).isEqualTo(1);
        assertThat(hp.get(0).getId()).isNotNull();
        assertThat(hp.get(0).getHearingId()).isEqualTo(ID);
    }

    @Test
    public void mapToHearingParts_withTwoSessionsToCreate_shouldReturnObjectWithTwoHearingParts() {
        val chpr = new CreateHearingRequest();
        chpr.setId(ID);
        chpr.setNumberOfSessions(2);

        val hp = new HearingMapper().mapToHearingParts(chpr);

        assertThat(hp.size()).isEqualTo(2);
        assertThat(hp.get(0).getId()).isNotNull();
        assertThat(hp.get(0).getHearingId()).isEqualTo(ID);
        assertThat(hp.get(1).getId()).isNotNull();
        assertThat(hp.get(1).getHearingId()).isEqualTo(ID);
    }

    @Test
    public void mapToHearingParts_fromCreateHearingPartRequestWithZeroSessionToCreate_shouldReturnObjectWithoutHearingParts() {
        val chpr = new CreateHearingRequest();
        chpr.setId(ID);
        chpr.setNumberOfSessions(0);

        val hp = new HearingMapper().mapToHearingParts(chpr);

        assertThat(hp.size()).isEqualTo(0);
    }
}
