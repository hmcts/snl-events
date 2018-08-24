package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.interfaces.SimpleDictonarySettable;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SimpleDictionaryData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReferenceDataService {

    @Autowired
    CaseTypeRepository caseTypeRepository;

    @Autowired
    SessionTypeRepository sessionTypeRepository;

    @Autowired
    HearingTypeRepository hearingTypeRepository;

    @Autowired
    RoomTypeRepository roomTypeRepository;

    public List<SimpleDictionaryData> getCaseTypes() {
        return getAllAsSimpleDictionaryData(caseTypeRepository);
    }

    public List<SimpleDictionaryData> getSessionTypes() {
        return getAllAsSimpleDictionaryData(sessionTypeRepository);
    }

    public List<SimpleDictionaryData> getHearingTypes() {
        return getAllAsSimpleDictionaryData(hearingTypeRepository);
    }

    public List<SimpleDictionaryData> getRoomTypes() {
        return getAllAsSimpleDictionaryData(roomTypeRepository);
    }

    private final Function<SimpleDictonarySettable, SimpleDictionaryData> toSimpleDictionaryData = (SimpleDictonarySettable sds) -> {
        SimpleDictionaryData response = new SimpleDictionaryData(sds.getCode(), sds.getDescription());

        return response;
    };

    private <T extends SimpleDictonarySettable> List<SimpleDictionaryData> getAllAsSimpleDictionaryData(JpaRepository<T, String> repository) {
        return repository
            .findAll()
            .stream()
            .map(toSimpleDictionaryData)
            .collect(Collectors.toList());
    }
}
