package queueless.exception;

/**
 * Exception de base du système QueueLess.
 * Toutes les exceptions métier héritent de cette classe.
 * Démontre : hiérarchie d'exceptions personnalisées.
 */
public class QueuelessException extends Exception {

    private final int errorCode;

    public QueuelessException(String message) {
        super(message);
        this.errorCode = 0;
    }

    public QueuelessException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public QueuelessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
