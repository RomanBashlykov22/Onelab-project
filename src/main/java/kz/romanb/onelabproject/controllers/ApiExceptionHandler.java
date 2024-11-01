package kz.romanb.onelabproject.controllers;

import jakarta.persistence.PersistenceException;
import kz.romanb.onelabproject.exceptions.AuthException;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.exceptions.NotEnoughMoneyException;
import kz.romanb.onelabproject.exceptions.RegistrationException;
import kz.romanb.onelabproject.models.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
            RegistrationException.class,
            DBRecordNotFoundException.class,
            UsernameNotFoundException.class,
            NotEnoughMoneyException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorDto> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest()
                .body(
                        ErrorDto.builder()
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .description(e.getMessage())
                                .build()
                );
    }

    @ExceptionHandler({AuthException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorDto> handleAuthException(Exception e){
        HttpStatus httpStatus;
        if(e instanceof AccessDeniedException){
            httpStatus = HttpStatus.FORBIDDEN;
        }
        else {
            httpStatus = getStatus(e.getClass());
        }
        return ResponseEntity.status(httpStatus)
                .body(
                        ErrorDto.builder()
                                .error(httpStatus.getReasonPhrase())
                                .description(e.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleNonValidData(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<String> errors = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(
                        ErrorDto.builder()
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .description(errors.toString())
                                .build()
                );
    }

    @ExceptionHandler({PersistenceException.class, Exception.class})
    public ResponseEntity<ErrorDto> handleInternalServerErrors(PersistenceException e) {
        HttpStatus httpStatus = getStatus(e.getClass());
        return ResponseEntity.status(httpStatus)
                .body(
                        ErrorDto.builder()
                                .error(httpStatus.getReasonPhrase())
                                .description(e.getMessage())
                                .build()
                );
    }

    private HttpStatus getStatus(Class<?> clazz){
        ResponseStatus responseStatus = clazz.getAnnotation(ResponseStatus.class);
        if(responseStatus != null){
            return responseStatus.value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
