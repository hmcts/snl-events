package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class PersonServiceTests {
    @TestConfiguration
    static class PersonServiceTestContextConfiguration {
        @Bean
        public PersonService personService() {
            return new PersonService();
        }
    }

    @Autowired
    PersonService personService;

    @MockBean
    PersonRepository personRepository;

    @Test
    public void getPersons_returnsPersonFromRepository() {
        val persons = createPersons();
        when(personRepository.findAll()).thenReturn(persons);
        val returnedPersons= personService.getPersons();

        assertThat(returnedPersons).isEqualTo(persons);
    }

    @Test
    public void getPersonsByType_returnsPersonFromRepository() {
        val persons = createPersons();
        when(personRepository.findPeopleByPersonTypeEqualsIgnoreCase(anyString()))
            .thenReturn(persons);
        val returnedPersons= personService.getPersonByType("");

        assertThat(returnedPersons).isEqualTo(persons);
    }

    private List<Person> createPersons() {
        return new ArrayList<>(Arrays.asList(new Person()));
    }
}
