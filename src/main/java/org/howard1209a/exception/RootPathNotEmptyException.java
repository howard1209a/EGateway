package org.howard1209a.exception;

public class RootPathNotEmptyException extends ServerException{
    public RootPathNotEmptyException() {
    }

    public RootPathNotEmptyException(String message) {
        super(message);
    }
}
