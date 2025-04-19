package com.msn.msncars.company.exception;

public class CompanyNotFoundException extends CompanyException {
    public CompanyNotFoundException(String message, Throwable cause) { super (message, cause);}

    public CompanyNotFoundException(String message) {super(message);}
}
