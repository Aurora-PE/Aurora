package ro.unibuc.prodeng.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateActionException
    extends RuntimeException {

    public DuplicateActionException(String message) {
        super(message);
    }
}