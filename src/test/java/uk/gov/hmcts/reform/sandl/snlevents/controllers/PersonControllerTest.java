package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.common.OurMockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.service.PersonService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(PersonController.class)
public class PersonControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    OurMockMvc mvc;

    @Before
    public void init() {
        mvc = new OurMockMvc(mockMvc, objectMapper);
    }

    @Test
    public void fetchAllPersons_returnsPersonsFromService() throws Exception {
        val persons = createPersons();
        when(personService.getPersons()).thenReturn(persons);

        val response = mvc.getAndMapResponse("/person", new TypeReference<List<Person>>(){});
        assertThat(response).isEqualTo(persons);
    }

    private List<Person> createPersons() {
        return new ArrayList<>(Arrays.asList(createPerson()));
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
