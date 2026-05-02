package queueless.exception;

/**
 * Exception lancée lors d'un problème d'authentification ou d'autorisation.
 * Hérite de QueuelessException – démontre l'héritage d'exceptions.
 */
public class AuthException extends QueuelessException {

    public AuthException(String message) {
        super(message, 401);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
