package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.Utils;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.BaseReferenceData;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.RoomType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.SessionType;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.CaseTypeWithHearingTypesResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SimpleDictionaryData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ReferenceDataServiceTest {
    public static final String DEFAULT_CODE = "c1";
    public static final String DEFAULT_DESCRIPTION = "desc";
    @InjectMocks
    ReferenceDataService service;

    @Mock
    CaseTypeRepository caseTypeRepository;
    @Mock
    SessionTypeRepository sessionTypeRepository;
    @Mock
    HearingTypeRepository hearingTypeRepository;
    @Mock
    RoomTypeRepository roomTypeRepository;

    @Test
    public void getSessionTypes_returnsSessionTypesFromRepository() {
        List<SessionType> dbStubbedValues = createSessionTypes();
        when(sessionTypeRepository.findAll()).thenReturn(dbStubbedValues);

        List<SimpleDictionaryData> expected = getSimpleDictionaryData(dbStubbedValues);

        val valuesToCheck = service.getSessionTypes();
        assertThat(valuesToCheck).isEqualTo(expected);
    }

    @Test
    public void getHearingTypes_returnsSessionTypesFromRepository() {
        val dbStubbedValues = createHearingTypes();
        when(hearingTypeRepository.findAll()).thenReturn(dbStubbedValues);

        List<SimpleDictionaryData> expected = getSimpleDictionaryData(dbStubbedValues);

        val valuesToCheck = service.getHearingTypes();
        assertThat(valuesToCheck).isEqualTo(expected);
    }

    @Test
    public void getRoomTypes_returnsRoomTypesFromRepository() {
        val dbStubbedValues = createRoomTypes();
        when(roomTypeRepository.findAll()).thenReturn(dbStubbedValues);

        List<SimpleDictionaryData> expected = getSimpleDictionaryData(dbStubbedValues);

        val valuesToCheck = service.getRoomTypes();
        assertThat(valuesToCheck).isEqualTo(expected);
    }

    @Test
    public void getCaseTypes_returnsCaseTypesFromRepository() {
        val dbStubbedValues = createCaseTypes();
        when(caseTypeRepository.findAll()).thenReturn(dbStubbedValues);

        val valuesToCheck = service.getCaseTypes();
        assertThat(valuesToCheck).isEqualTo(createCaseTypesResponse());
    }

    private List<SessionType> createSessionTypes() {
        return Arrays.asList(new SessionType());
    }

    private List<RoomType> createRoomTypes() {
        return Arrays.asList(new RoomType());
    }

    private List<HearingType> createHearingTypes() {
        return Arrays.asList(new HearingType());
    }

    private List<CaseType> createCaseTypes() {
        CaseType rv = new CaseType(DEFAULT_CODE, DEFAULT_DESCRIPTION);
        rv.addHearingType(new HearingType(DEFAULT_CODE, DEFAULT_DESCRIPTION));

        return Arrays.asList(rv);
    }

    private List<CaseTypeWithHearingTypesResponse> createCaseTypesResponse() {
        return Utils.getCaseTypeWithHearingTypesResponses(DEFAULT_CODE, DEFAULT_DESCRIPTION);
    }

    private List<SimpleDictionaryData> getSimpleDictionaryData(List<? extends BaseReferenceData> dbStubbedValues) {
        return dbStubbedValues.stream()
            .map(val -> new SimpleDictionaryData(val.getCode(), val.getDescription()))
            .collect(Collectors.toList());
    }
}
