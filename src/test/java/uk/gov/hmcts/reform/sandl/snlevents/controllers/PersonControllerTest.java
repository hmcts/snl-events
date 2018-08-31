package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.EventsMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.config.TestConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;
import uk.gov.hmcts.reform.sandl.snlevents.service.PersonService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(PersonController.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc(secure = false)
public class PersonControllerTest {

    @MockBean
    private PersonService personService;
    @Autowired
    private EventsMockMvc mvc;
    @MockBean
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @Test
    public void fetchAllPersons_returnsPersonsFromService() throws Exception {
        val persons = createPersons();
        when(personService.getPersons()).thenReturn(persons);

        val response = mvc.getAndMapResponse("/person", new TypeReference<List<Person>>(){});
        assertThat(response).isEqualTo(persons);
    }

    private List<Person> createPersons() {
        return Arrays.asList(createPerson());
    }

    private Person createPerson() {
        return new Person();
    }

    @Test
    public void fetchAllJudges_returnsJudgesFromService() throws Exception {
        val persons = createPersons();
        when(personService.getPersonByType("judge")).thenReturn(persons);

        val response = mvc.getAndMapResponse("/person?personType=judge", new TypeReference<List<Person>>(){});
        assertThat(response).isEqualTo(persons);
    }
}
