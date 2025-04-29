package com.msn.msncars.company.exception;

public abstract class CompanyException extends RuntimeException {
    public CompanyException(String message, Throwable cause) { super (message, cause);}

    public CompanyException(String message) {super(message);}
}
