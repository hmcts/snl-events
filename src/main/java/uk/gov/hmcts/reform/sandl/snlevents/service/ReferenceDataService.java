package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.interfaces.SimpleDictionarySettable;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.CaseTypeWithHearingTypesResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SimpleDictionaryData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.CaseTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReferenceDataService {

    private final Function<SimpleDictionarySettable, SimpleDictionarySettable> toSimpleDictionaryData =
        (SimpleDictionarySettable sds) -> {
            SimpleDictionarySettable response = new SimpleDictionaryData(sds.getCode(), sds.getDescription());

            return response;
        };
    @Autowired
    CaseTypeRepository caseTypeRepository;
    @Autowired
    SessionTypeRepository sessionTypeRepository;
    @Autowired
    HearingTypeRepository hearingTypeRepository;
    @Autowired
    RoomTypeRepository roomTypeRepository;

    public List<CaseTypeWithHearingTypesResponse> getCaseTypes() {
        return caseTypeRepository
            .findAll()
            .stream()
            .map(caseType -> {
                CaseTypeWithHearingTypesResponse mappedTo = new CaseTypeWithHearingTypesResponse();
                final Set<SimpleDictionarySettable> associatedHearingTypes = caseType.getHearingTypes()
                    .stream()
                    .map(toSimpleDictionaryData)
                    .collect(Collectors.toSet());

                mappedTo.setHearingTypes(associatedHearingTypes);
                mappedTo.setCode(caseType.getCode());
                mappedTo.setDescription(caseType.getDescription());
                return mappedTo;
            })
            .collect(Collectors.toList());
    }

    public List<SimpleDictionarySettable> getSessionTypes() {
        return getAllAsSimpleDictionaryData(sessionTypeRepository);
    }

    public List<SimpleDictionarySettable> getHearingTypes() {
        return getAllAsSimpleDictionaryData(hearingTypeRepository);
    }

    public List<SimpleDictionarySettable> getRoomTypes() {
        return getAllAsSimpleDictionaryData(roomTypeRepository);
    }

    @SuppressWarnings({"Indentation", "MethodParamPad"})
    private <T extends SimpleDictionarySettable> List<SimpleDictionarySettable> getAllAsSimpleDictionaryData
        (JpaRepository<T, String> repository) {
        return repository
            .findAll()
            .stream()
            .map(toSimpleDictionaryData)
            .collect(Collectors.toList());
    }
}
