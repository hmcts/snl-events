package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class PersonServiceTest {
    @InjectMocks
    PersonService personService;

    @Mock
    PersonRepository personRepository;

    @Test
    public void getPersons_returnsPersonFromRepository() {
        val persons = createPersons();
        when(personRepository.findAll()).thenReturn(persons);
        val returnedPersons = personService.getPersons();

        assertThat(returnedPersons).isEqualTo(persons);
    }

    @Test
    public void getPersonsByType_returnsPersonFromRepository() {
        val persons = createPersons();
        when(personRepository.findPeopleByPersonTypeEqualsIgnoreCase(anyString()))
            .thenReturn(persons);
        val returnedPersons = personService.getPersonByType("");

        assertThat(returnedPersons).isEqualTo(persons);
    }

    private List<Person> createPersons() {
        return Arrays.asList(new Person());
    }
}
