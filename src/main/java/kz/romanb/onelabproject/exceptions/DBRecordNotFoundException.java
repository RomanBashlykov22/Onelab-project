package kz.romanb.onelabproject.exceptions;

public class DBRecordNotFoundException extends RuntimeException {
    public DBRecordNotFoundException(String message) {
        super(message);
    }
}
