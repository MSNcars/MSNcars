package com.msn.msncars.car.exception;

public abstract class CarException extends RuntimeException {
    public CarException(String message, Throwable cause) { super (message, cause);}

    public CarException(String message) {super(message);}
}
