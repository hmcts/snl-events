package uk.gov.hmcts.reform.sandl.snlevents.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseAssertions {
    private final ObjectMapper objectMapper;

    public ResponseAssertions(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void assertResponseEquals(MockHttpServletResponse response, Object expected, TypeReference typeReference) throws IOException {
        val r = objectMapper.readValue(response.getContentAsString(), typeReference);
        assertThat(r).isEqualToComparingFieldByFieldRecursively(expected);
    }

    public void assertResponseEquals(MockHttpServletResponse response, Object expected, Class<?> _class) throws IOException {
        val r = objectMapper.readValue(response.getContentAsString(), _class);
        assertThat(r).isEqualToComparingFieldByFieldRecursively(expected);
    }
}
