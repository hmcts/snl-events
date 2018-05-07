package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.service.PersonService;

import java.util.List;

@RestController()
@RequestMapping("/persons")
public class PersonController {

    @Autowired
    PersonService personService;

    @RequestMapping(path = "", method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody
    List<Person> fetchAllPersons() {
        return personService.getPersons();
    }


    @RequestMapping(path = "", params = "personType", method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody
    List<Person> fetchAllJudges(@RequestParam("personType") String personType) {
        return personService.getPeopleByType(personType);
    }
}
