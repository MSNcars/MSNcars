package com.msn.msncars.listing.exception;

public abstract class ListingException extends RuntimeException {
    public ListingException(String message, Throwable cause) { super (message, cause);}

    public ListingException(String message) {super(message);}
}
