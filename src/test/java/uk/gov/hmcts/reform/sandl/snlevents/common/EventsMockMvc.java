package uk.gov.hmcts.reform.sandl.snlevents.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AllArgsConstructor
@Component
public class EventsMockMvc {
    @Getter
    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

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
}
