package uk.gov.hmcts.reform.sandl.snlevents.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

@Service
public class HearingPartService {

    @Autowired
    HearingPartRepository hearingPartRepository;

    @Autowired
    SessionRepository sessionRepository;

    public List<HearingPart> getAllHearingParts() {
        return hearingPartRepository.findAll();
    }

    public HearingPart save(HearingPart hearingPart) {
        return hearingPartRepository.save(hearingPart);
    }

    public HearingPart assignHearingPartToSession(UUID hearingPartId, UUID sessionId) {
        HearingPart hearingPart = hearingPartRepository.findOne(hearingPartId);

        Session session = sessionRepository.findOne(sessionId);

        hearingPart.setSession(session);

        return hearingPartRepository.save(hearingPart);
    }
}
