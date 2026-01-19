package org.havryliuk.loadbalancer.exception;

public class NoAvailableServerException extends RuntimeException {

    public NoAvailableServerException() {
        super("No healthy backend server available");
    }
}
