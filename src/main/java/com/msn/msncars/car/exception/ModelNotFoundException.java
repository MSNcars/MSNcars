package com.msn.msncars.car.exception;

public class ModelNotFoundException extends CarException {
    public ModelNotFoundException(String message, Throwable cause) { super (message, cause);}

    public ModelNotFoundException(String message) {super(message);}
}
