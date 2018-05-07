package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;

import java.util.List;

@Service
public class PersonService {

    @Autowired
    PersonRepository personRepository;

    public List<Person> getPersons() {
        return personRepository.findAll();
    }

    public List<Person> getPersonByType(String personType) {
        return personRepository.findPeopleByPersonTypeEqualsIgnoreCase(personType);
    }
}
