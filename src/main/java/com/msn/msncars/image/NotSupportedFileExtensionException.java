package com.msn.msncars.image;

public class NotSupportedFileExtensionException extends RuntimeException{
    public NotSupportedFileExtensionException(String errorMessage) {
        super(errorMessage);
    }

    public NotSupportedFileExtensionException(String errorMessage, Exception e) {
        super(errorMessage, e);
    }
}
