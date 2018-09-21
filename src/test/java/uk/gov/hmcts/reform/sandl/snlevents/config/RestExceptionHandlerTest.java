package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ValidationErrorDetails;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class RestExceptionHandlerTest {

    private RestExceptionHandler restExceptionHandler = new RestExceptionHandler();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    MethodArgumentNotValidException ex;

    @Mock
    HttpHeaders headers;

    @Mock
    WebRequest request;

    @Test
    public void handleMethodArgumentNotValid_shouldTransformResponseCorrectly() {
        FieldError fieldError = new FieldError("createHearingPart", "duration",
            "Duration is shorter than 1 minutes");

        Mockito.when(ex.getBindingResult().getFieldErrors()).thenReturn(Arrays.asList(fieldError));

        ResponseEntity responseEntity = restExceptionHandler
            .handleMethodArgumentNotValid(ex, headers, HttpStatus.BAD_REQUEST, request);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));

        ValidationErrorDetails validationErrorDetails = (ValidationErrorDetails) responseEntity.getBody();
        List<ValidationErrorDetails.ErrorDetails> errorDetailsList = validationErrorDetails.getErrorDetailsList();

        assertThat(errorDetailsList.size(), is(1));

        ValidationErrorDetails.ErrorDetails errorDetails = errorDetailsList.get(0);

        assertThat(errorDetails.getErrorMessage(), is("Duration is shorter than 1 minutes"));
        assertThat(errorDetails.getField(), is("duration"));
    }
}
