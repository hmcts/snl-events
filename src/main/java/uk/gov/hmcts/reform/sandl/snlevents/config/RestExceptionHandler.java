package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ValidationErrorDetails;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.OptimisticLockException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    protected ResponseEntity<Object> handleOptimisticLockException(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        List<ValidationErrorDetails.ErrorDetails> errorMessages = ex.getBindingResult().getFieldErrors().stream()
            .map(ValidationErrorDetails::fromFieldError)
            .collect(Collectors.toList());

        ValidationErrorDetails validationErrorDetails = new ValidationErrorDetails(errorMessages);
        return new ResponseEntity<>(validationErrorDetails, HttpStatus.BAD_REQUEST);
    }
}
