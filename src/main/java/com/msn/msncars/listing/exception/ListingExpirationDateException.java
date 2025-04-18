package com.msn.msncars.listing.exception;

public class ListingExpirationDateException extends ListingException {
   public ListingExpirationDateException(String message, Throwable cause) { super(message, cause);}

    public ListingExpirationDateException(String message) { super(message); }
}
