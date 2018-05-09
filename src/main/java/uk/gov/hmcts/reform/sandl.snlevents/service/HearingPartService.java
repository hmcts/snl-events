package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;

import java.util.List;

@Service
public class HearingPartService {

    @Autowired
    HearingPartRepository hearingPartRepository;

    public List<HearingPart> getAllHearingParts() {
        return hearingPartRepository.findAll();
    }

    public HearingPart save(HearingPart hearingPart) {
        return hearingPartRepository.save(hearingPart);
    }
}
