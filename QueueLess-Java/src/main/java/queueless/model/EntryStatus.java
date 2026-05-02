package queueless.model;

/**
 * Enum représentant le statut d'une entrée dans la file d'attente.
 * Remplace l'ENUM MySQL ('waiting', 'served', 'cancelled').
 */
public enum EntryStatus {
    WAITING("waiting"),
    SERVED("served"),
    CANCELLED("cancelled");

    private final String dbValue;

    EntryStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static EntryStatus fromDbValue(String value) {
        for (EntryStatus s : values()) {
            if (s.dbValue.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Statut inconnu : " + value);
    }
}
