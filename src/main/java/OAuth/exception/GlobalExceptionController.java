package OAuth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionController {
    @ExceptionHandler(value = CustomException.class)
    protected ResponseEntity<OAuth.exception.ErrorResponse> unAuthenticatedException(CustomException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage()));
    }
}
