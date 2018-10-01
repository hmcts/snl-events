package uk.gov.hmcts.reform.sandl.snlevents.mappers;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateHearingPartRequest;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class HearingPartMapperTests {
    private static final UUID ID = UUID.randomUUID();
    private static final String CASE_NUMBER = "some-case-number";
    private static final String CASE_TITLE = "some-case-title";
    private static final String CASE_TYPE_CODE = "case-type-code";
    private static final String HEARING_TYPE_CODE = "hearing-type-code";
    private static final Duration DURATION = Duration.ofMinutes(30);
    private static final OffsetDateTime SCHEDULE_START = OffsetDateTime.now();
    private static final OffsetDateTime SCHEDULE_END = OffsetDateTime.now();
    private static final Priority PRIORITY = Priority.Medium;
    private static final UUID RESERVED_JUDGE_ID = UUID.randomUUID();
    private static final String COMMUNICATION_FACILITATOR = "some communication facilitator";
    private static final UUID USER_TRANSACTION_ID = UUID.randomUUID();

    private static final CaseType CASE_TYPE = new CaseType(CASE_TYPE_CODE, "case-type-desc");
    private static final HearingType HEARING_TYPE = new HearingType(HEARING_TYPE_CODE, "hearing-type-desc");

    @Mock
    private HearingTypeRepository hearingTypeRepository;

    @Mock
    private CaseTypeRepository caseTypeRepository;

    @Test
    public void mapToHearingPart_fromCreateHearingPartRequest_shouldSetProperties() {
        when(caseTypeRepository.findOne(CASE_TYPE_CODE)).thenReturn(CASE_TYPE);
        when(hearingTypeRepository.findOne(HEARING_TYPE_CODE)).thenReturn(HEARING_TYPE);

        val chpr = new CreateHearingPartRequest();
            chpr.setId(ID);
            chpr.setCaseNumber(CASE_NUMBER);
            chpr.setCaseTitle(CASE_TITLE);
            chpr.setCaseTypeCode(CASE_TYPE_CODE);
            chpr.setHearingTypeCode(HEARING_TYPE_CODE);
            chpr.setDuration(DURATION);
            chpr.setScheduleStart(SCHEDULE_START);
            chpr.setScheduleEnd(SCHEDULE_END);
            chpr.setPriority(PRIORITY);
            chpr.setReservedJudgeId(RESERVED_JUDGE_ID);
            chpr.setCommunicationFacilitator(COMMUNICATION_FACILITATOR);
            chpr.setUserTransactionId(USER_TRANSACTION_ID);

        val hp = new HearingPartMapper().mapToHearingPart(chpr, caseTypeRepository, hearingTypeRepository);

        assertThat(hp.getId()).isEqualTo(ID);
        assertThat(hp.getCaseNumber()).isEqualTo(CASE_NUMBER);
        assertThat(hp.getCaseTitle()).isEqualTo(CASE_TITLE);
        assertThat(hp.getCaseType()).isEqualTo(CASE_TYPE);
        assertThat(hp.getHearingType()).isEqualTo(HEARING_TYPE);
        assertThat(hp.getDuration()).isEqualTo(DURATION);
        assertThat(hp.getScheduleStart()).isEqualTo(SCHEDULE_START);
        assertThat(hp.getScheduleEnd()).isEqualTo(SCHEDULE_END);
        assertThat(hp.getPriority()).isEqualTo(PRIORITY);
        assertThat(hp.getReservedJudgeId()).isEqualTo(RESERVED_JUDGE_ID);
        assertThat(hp.getCommunicationFacilitator()).isEqualTo(COMMUNICATION_FACILITATOR);
    }
}
