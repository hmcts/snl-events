package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.springframework.context.support.DefaultMessageSourceResolvable;
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
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ErrorValidationErrors;

import javax.persistence.OptimisticLockException;
import java.util.List;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    protected ResponseEntity<Object> handleOptimisticLockException(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ErrorValidationErrors.ErrorDetails> errorMessages = ex.getBindingResult().getFieldErrors().stream()
            .map(ErrorValidationErrors::fromFieldError)
            .collect(Collectors.toList());

        ErrorValidationErrors errorValidationErrors = new ErrorValidationErrors(errorMessages);
        return new ResponseEntity<>(errorValidationErrors, HttpStatus.BAD_REQUEST);
    }
}
