package com.msn.msncars.listing.exception;

public class ListingExpiredException extends ListingException {
    public ListingExpiredException(String message, Throwable cause) {super(message, cause);}

    public ListingExpiredException(String message) {
        super(message);
    }
}
