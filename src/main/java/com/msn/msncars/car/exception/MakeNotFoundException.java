package com.msn.msncars.car.exception;

public class MakeNotFoundException extends CarException {
    public MakeNotFoundException(String message, Throwable cause) { super (message, cause);}

    public MakeNotFoundException(String message) {super(message);}
}
