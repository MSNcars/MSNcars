package com.msn.msncars.exception;

import com.msn.msncars.car.exception.MakeNotFoundException;
import com.msn.msncars.car.exception.ModelNotFoundException;
import com.msn.msncars.company.exception.CompanyNotFoundException;
import com.msn.msncars.listing.exception.ListingNotFoundException;
import com.msn.msncars.listing.exception.ListingRevokedException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler  {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(ListingNotFoundException.class)
    protected ResponseEntity<String> handleListingNotFoundException(ListingNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(CompanyNotFoundException.class)
    protected ResponseEntity<String> handleCompanyNotFoundException(CompanyNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(MakeNotFoundException.class)
    protected ResponseEntity<String> handleMakeNotFoundException(MakeNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ModelNotFoundException.class)
    protected ResponseEntity<String> handleModelNotFoundException(ModelNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ListingRevokedException.class)
    protected ResponseEntity<String> handleListingRevokedException(ListingRevokedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

}


