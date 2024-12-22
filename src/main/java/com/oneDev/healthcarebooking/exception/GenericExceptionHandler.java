package com.oneDev.healthcarebooking.exception;

import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.model.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GenericExceptionHandler {

    @ExceptionHandler({
            ApplicationException.class,
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse  handleApplicationException(HttpServletRequest req,
                                                                   HttpServletResponse resp,
                                                                   ApplicationException exception) {

        ExceptionType type = exception.getType();
        HttpStatus status = HttpStatus.resolve(type.getHttpCode());

        // Atur status HTTP pada response
        assert status != null;
        resp.setStatus(status.value());

        return  ErrorResponse.builder()
                .code(status.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleGenericException(HttpServletRequest req,
                                                              HttpServletResponse resp,
                                                              Exception exception) {
        log.error("Terjadi error. status code: {}error message: {}", HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        if (exception instanceof AccessDeniedException ||
                exception instanceof BadCredentialsException ||
                exception instanceof SignatureException ||
                exception instanceof ExpiredJwtException ||
                exception instanceof AuthenticationException ||
                exception instanceof InsufficientAuthenticationException
        ) {

            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ErrorResponse.builder()
                    .code(HttpStatus.FORBIDDEN.value())
                    .message(exception.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return ErrorResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(objectError -> {
            String fieldName = ((FieldError) objectError).getField();
            String errorMessage = objectError.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(errors.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
