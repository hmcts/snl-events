package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.StatusConfigResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.StatusConfigRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/status-config")
public class StatusConfigController {
    @Autowired
    private StatusConfigRepository statusConfigRepository;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StatusConfigResponse> getAllStatuses() {
        return statusConfigRepository.findAll()
            .stream()
            .map(StatusConfigResponse::new)
            .collect(Collectors.toList());
    }
}
