package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.HearingPartSessionRelationship;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class HearingPartService {

    @Autowired
    HearingPartRepository hearingPartRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    private RulesService rulesService;

    @Autowired
    private FactsMapper factsMapper;

    public List<HearingPart> getAllHearingParts() {
        return hearingPartRepository.findAll();
    }

    public HearingPart save(HearingPart hearingPart) {
        return hearingPartRepository.save(hearingPart);
    }

    public HearingPart assignHearingPartToSession(UUID hearingPartId,
                                                  HearingPartSessionRelationship assignment) throws IOException {
        HearingPart hearingPart = hearingPartRepository.findOne(hearingPartId);

        Session session = (assignment.getSessionId() == null) ? null :
            sessionRepository.findOne(assignment.getSessionId());

        hearingPart.setSession(session);
        hearingPart.setStart(assignment.getStart());

        String msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
        rulesService.postMessage(RulesService.UPSERT_HEARING_PART, msg);

        return hearingPartRepository.save(hearingPart);
    }
}
