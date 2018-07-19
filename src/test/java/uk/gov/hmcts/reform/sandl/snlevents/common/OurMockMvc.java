package uk.gov.hmcts.reform.sandl.snlevents.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//consider better name for class
@Component
public class OurMockMvc {
    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    public OurMockMvc(
        MockMvc mockMvc,
        ObjectMapper objectMapper
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public <T> T getAndMapResponse(String url, TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(getResponseAsString(url), typeReference);
    }

    public <T> T getAndMapResponse(String url, Class<T> clazz) throws Exception {
        return objectMapper.readValue(getResponseAsString(url), clazz);
    }

    private String getResponseAsString(String url) throws Exception {
        return mockMvc
            .perform(get(url))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }

    public <T> T putAndMapResponse(String url, String body, TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(putResponseAsString(url, body), typeReference);
    }

    public <T> T putAndMapResponse(String url, String body, Class<T> clazz) throws Exception {
        return objectMapper.readValue(putResponseAsString(url, body), clazz);
    }

    private String putResponseAsString(String url, String body) throws Exception {
        return mockMvc
            .perform(put(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }

    public MockMvc getMockMvc() {
        return mockMvc;
    }
}
