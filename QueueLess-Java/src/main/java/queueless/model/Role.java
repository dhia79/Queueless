package queueless.model;

/**
 * Enum représentant les rôles des utilisateurs dans le système QueueLess.
 * Utilise des minuscules pour correspondre directement aux valeurs de la base de données.
 */
public enum Role {
    customer,
    business,
    admin;

    public static Role fromString(String value) {
        try {
            return Role.valueOf(value.toLowerCase());
        } catch (Exception e) {
            return customer;
        }
    }
    
    // Pour compatibilité avec le code existant qui pourrait appeler ADMIN/BUSINESS/CUSTOMER
    public static final Role ADMIN = admin;
    public static final Role BUSINESS = business;
    public static final Role CUSTOMER = customer;
}
