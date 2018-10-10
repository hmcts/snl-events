package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingInfo;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;

import java.util.UUID;

@RestController
@RequestMapping("hearing")
public class HearingController {

    @Autowired
    private HearingRepository hearingRepository;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HearingInfo getHearingById(@PathVariable("id") UUID id) {
        return new HearingInfo(hearingRepository.findOne(id));
    }
}
