package com.msn.msncars.listing.exception;

public class ListingNotFoundException extends ListingException {
    public ListingNotFoundException(String message, Throwable cause) {super(message, cause);}

    public ListingNotFoundException(String message) {
        super(message);
    }
}
