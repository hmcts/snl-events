package uk.gov.hmcts.reform.sandl.snlevents.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//nottodo make this class autowire'able and pick better name for it
public class OurMockMvc {
    MockMvc mockMvc;

    ObjectMapper objectMapper;

    public OurMockMvc(
        @Autowired  MockMvc mockMvc,
        @Autowired  ObjectMapper objectMapper
    )
    {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public Object getAndMapResponse(String url, TypeReference typeReference) throws Exception {
        return objectMapper.readValue(getResponseAsString(url), typeReference);
    }

    public Object getAndMapResponse(String url, Class _class) throws Exception {
        return objectMapper.readValue(getResponseAsString(url), _class);
    }

    private String getResponseAsString(String url) throws Exception {
        return mockMvc
            .perform(get(url))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }

    public Object putAndMapResponse(String url, String body, TypeReference typeReference) throws Exception {
        return objectMapper.readValue(putResponseAsString(url, body), typeReference);
    }

    public Object putAndMapResponse(String url, String body, Class _class) throws Exception {
        return objectMapper.readValue(putResponseAsString(url, body), _class);
    }

    private String putResponseAsString(String url, String body) throws Exception {
        return mockMvc
            .perform(put(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }
}
