package queueless.exception;

/**
 * Exception lancée quand une entité demandée n'existe pas en base de données.
 * Hérite de QueuelessException.
 */
public class NotFoundException extends QueuelessException {

    public NotFoundException(String entityName, int id) {
        super(entityName + " introuvable (id=" + id + ")", 404);
    }

    public NotFoundException(String message) {
        super(message, 404);
    }
}
