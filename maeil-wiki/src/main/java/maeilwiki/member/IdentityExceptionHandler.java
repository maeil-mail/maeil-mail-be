package maeilwiki.member;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
class IdentityExceptionHandler {

    @ExceptionHandler(IdentityException.class)
    public ResponseEntity<Void> handleIdentityException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
