package uk.gov.hmcts.reform.sandl.snlevents.actions.hearingpart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces.RulesProcessable;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AmendScheduledListing;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

public class AmendScheduledListingAction extends Action implements RulesProcessable {

    protected AmendScheduledListing amendScheduledListing;
    protected HearingPart hearingPart;
    protected String previousHearingPart;
    protected String previousHearing;

    protected HearingPartRepository hearingPartRepository;
    protected EntityManager entityManager;

    @SuppressWarnings("squid:S00107") // we intentionally go around DI here as such the amount of parameters
    public AmendScheduledListingAction(AmendScheduledListing amendScheduledListing,
                                       HearingPartRepository hearingPartRepository,
                                       EntityManager entityManager,
                                       ObjectMapper objectMapper) {
        this.amendScheduledListing = amendScheduledListing;
        this.hearingPartRepository = hearingPartRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getAndValidateEntities() {
        hearingPart = hearingPartRepository.findOne(amendScheduledListing.getHearingPartId());

        if (hearingPart == null) {
            throw new SnlEventsException("Hearing part cannot be null!");
        }

        if (!hearingPart.getStatus().getStatus().equals(Status.Listed)) {
            throw new SnlEventsException("You cannot amend a hearing part that is not listed");
        }
    }

    @Override
    public UUID[] getAssociatedEntitiesIds() {
        return new UUID[] {hearingPart.getId()};
    }

    @Override
    public void act() {
        try {
            previousHearingPart = objectMapper.writeValueAsString(hearingPart);
        } catch (JsonProcessingException e) {
            throw new SnlEventsException(e);
        }
        hearingPart.setVersion(amendScheduledListing.getHearingPartVersion());

        val localTime = LocalTime.parse(amendScheduledListing.getStartTime(),
            DateTimeFormatter.ofPattern(AmendScheduledListing.TIME_FORMAT));
        val hour = localTime.get(ChronoField.CLOCK_HOUR_OF_DAY);
        val minute = localTime.get(ChronoField.MINUTE_OF_HOUR);

        hearingPart.setStart(hearingPart.getStart().withHour(hour).withMinute(minute));
        hearingPartRepository.save(hearingPart);
    }

    @Override //Done although hearing and session for user transactionDAta are not needed
    public List<UserTransactionData> generateUserTransactionData() {
        List<UserTransactionData> userTransactionDataList = new ArrayList<>();

        userTransactionDataList.add(new UserTransactionData("hearingPart",
            hearingPart.getId(),
            previousHearingPart,
            "update",
            "update",
            1)
        );

        return userTransactionDataList;
    }

    @Override
    public List<FactMessage> generateFactMessages() {
        String msg = factsMapper.mapHearingPartToRuleJsonMessage(hearingPart);
        return Collections.singletonList(new FactMessage(RulesService.UPSERT_HEARING_PART, msg));
    }

    @Override
    public UUID getUserTransactionId() {
        return amendScheduledListing.getUserTransactionId();
    }
}
