package kz.romanb.onelabproject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DBRecordNotFoundException extends RuntimeException {
    public DBRecordNotFoundException(String message) {
        super(message);
    }
}
