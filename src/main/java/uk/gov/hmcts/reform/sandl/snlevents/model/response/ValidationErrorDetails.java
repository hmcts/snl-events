package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.validation.FieldError;

import java.io.Serializable;
import java.util.List;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ValidationErrorDetails implements Serializable {
    private List<ErrorDetails> errorDetailsList;

    public static ErrorDetails fromFieldError(FieldError fieldError) {
        return new ErrorDetails(fieldError.getField(), fieldError.getDefaultMessage());
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ErrorDetails implements Serializable {
        private String field;
        private String errorMessage;
    }
}


