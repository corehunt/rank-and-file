package com.rankandfile.dataloader.exception;

/**
 * Custom exception to indicate that the API rate limit has been exceeded (HTTP 429).
 */
public class TooManyRequestsException extends Exception {
    /**
     * Constructs a new TooManyRequestsException with the specified detail message.
     *
     * @param message The detail message.
     */
    public TooManyRequestsException(String message) {
        super(message);
    }

    /**
     * Constructs a new TooManyRequestsException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause   The cause (which is saved for later retrieval by the Throwable.getCause() method).
     */
    public TooManyRequestsException(String message, Throwable cause) {
        super(message, cause);
    }
}
