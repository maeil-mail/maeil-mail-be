package maeilwiki.auth;

import lombok.extern.slf4j.Slf4j;
import maeilwiki.member.application.MemberIdentityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
class MemberIdentityExceptionHandler {

    @ExceptionHandler(MemberIdentityException.class)
    public ResponseEntity<Void> handleIdentityException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
