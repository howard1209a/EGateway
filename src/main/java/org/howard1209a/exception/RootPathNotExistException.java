package org.howard1209a.exception;

public class RootPathNotExistException extends ServerException{
    public RootPathNotExistException() {
    }

    public RootPathNotExistException(String message) {
        super(message);
    }
}
