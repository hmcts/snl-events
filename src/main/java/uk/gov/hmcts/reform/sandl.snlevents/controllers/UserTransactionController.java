package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.service.UserTransactionService;

import java.util.UUID;

@RestController()
@RequestMapping("/user-transaction")
public class UserTransactionController {

    @Autowired
    private UserTransactionService userTransactionService;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserTransaction getUserTransactionById(@PathVariable("id") UUID id) {
        return userTransactionService.getUserTransactionById(id);
    }
}
