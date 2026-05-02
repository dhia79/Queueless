package queueless.exception;

/**
 * Exception lancée lors d'une tentative d'insertion d'un doublon.
 * (ex: email déjà enregistré, utilisateur déjà dans la file)
 * Hérite de QueuelessException.
 */
public class DuplicateEntryException extends QueuelessException {

    public DuplicateEntryException(String message) {
        super(message, 409);
    }
}
