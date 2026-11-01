package com.tutorial.infrastructure.adapter.in.web;

import com.tutorial.domain.CustomerNotFoundException;
import com.tutorial.domain.DuplicateEmailException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Translates exceptions into RFC 9457 problem responses.
 * Extends ResponseEntityExceptionHandler so malformed JSON, wrong parameter types,
 * unsupported methods and similar Spring MVC errors also use the problem format.
 */
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomerNotFoundException.class)
    ProblemDetail handleNotFound(CustomerNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ProblemDetail handleDuplicateEmail(DuplicateEmailException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Safety net for a unique-constraint race that slips past the service-level check. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The request conflicts with existing data");
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    /** Locked, disabled or expired accounts: still 401, without revealing the reason. */
    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentication failed");
    }

    /** Let Spring Security produce the 403; do not turn it into a 500 below. */
    @ExceptionHandler(AccessDeniedException.class)
    void rethrowAccessDenied(AccessDeniedException ex) {
        throw ex;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return handleExceptionInternal(ex, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details),
                headers, HttpStatus.BAD_REQUEST, request);
    }

    /** Invalid @RequestParam / @PathVariable constraints such as page=-1 or size=1000. */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String details = ex.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(e -> r.getMethodParameter().getParameterName() + ": " + e.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return handleExceptionInternal(ex, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details),
                headers, HttpStatus.BAD_REQUEST, request);
    }
}
