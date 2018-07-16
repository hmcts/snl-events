package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.service.PersonService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(PersonController.class)
public class PersonControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private PersonService personService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void fetchAllPersons_returnsPersonsFromService() throws Exception {
        val persons = createPersons();

        when(personService.getPersons()).thenReturn(persons);

        val response = mvc
            .perform(get("/person"))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        val r = objectMapper.readValue(response.getContentAsString(), new TypeReference<List<Person>>(){});

        assertThat(r).isEqualTo(persons);
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

        val response = mvc
            .perform(get("/person?personType=judge"))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        val r = objectMapper.readValue(response.getContentAsString(), new TypeReference<List<Person>>(){});

        assertThat(r).isEqualTo(persons);
    }
}
