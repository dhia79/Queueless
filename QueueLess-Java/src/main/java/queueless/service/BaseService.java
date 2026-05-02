package queueless.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base abstraite pour les services.
 */
public abstract class BaseService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    // ── Méthode utilitaire partagée ───────────────────────────────────────
    protected void log(String tag, String message) {
        String timestamp = LocalDateTime.now().format(FMT);
        System.out.printf("[%s][%s] %s%n", timestamp, tag, message);
    }

    /**
     * Méthode abstraite : chaque service expose son nom.
     * Démontre le polymorphisme via la surcharge obligatoire.
     */
    public abstract String getServiceName();
}
