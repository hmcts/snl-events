package uk.gov.hmcts.reform.sandl.snlevents.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AllArgsConstructor
@TestComponent
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
        return callAndMapResponse(put(url), body, typeReference);
    }

    public <T> T putAndMapResponse(String url, String body, Class<T> clazz) throws Exception {
        return callAndMapResponse(put(url), body, clazz);
    }

    private <T> T callAndMapResponse(MockHttpServletRequestBuilder requestBuilder,
                                    String body,
                                    TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(callResponseAsString(requestBuilder, body), typeReference);
    }

    public <T> T callAndMapResponse(MockHttpServletRequestBuilder requestBuilder,
                                    Object body,
                                    TypeReference<T> typeReference) throws Exception {
        val bodyAsString = objectMapper.writeValueAsString(body);

        return objectMapper.readValue(callResponseAsString(requestBuilder, bodyAsString), typeReference);
    }

    public <T> T callAndMapResponse(MockHttpServletRequestBuilder requestBuilder,
                                    Object body,
                                    Class<T> clazz) throws Exception {
        val bodyAsString = objectMapper.writeValueAsString(body);

        return objectMapper.readValue(callResponseAsString(requestBuilder, bodyAsString), clazz);
    }

    public <T> T callAndMapResponse(MockHttpServletRequestBuilder requestBuilder,
                                    String body,
                                    Class<T> clazz) throws Exception {
        return objectMapper.readValue(callResponseAsString(requestBuilder, body), clazz);
    }

    private String callResponseAsString(MockHttpServletRequestBuilder requestBuilder, String body) throws Exception {
        return mockMvc
            .perform(requestBuilder.contentType(MediaType.APPLICATION_JSON_VALUE).content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }
}
