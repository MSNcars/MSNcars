package com.msn.msncars.listing.exception;

public class ListingRevokedException extends ListingException {
    public ListingRevokedException(String message, Throwable cause) {super(message, cause);}

    public ListingRevokedException(String message) {
        super(message);
    }
}
