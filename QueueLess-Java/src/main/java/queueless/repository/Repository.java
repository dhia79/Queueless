package queueless.repository;

import queueless.exception.QueuelessException;

import java.util.List;
import java.util.Optional;

/**
 * Interface générique définissant les opérations CRUD de base.
 * Démontre : interface, generics (type paramétré T).
 *
 * @param <T> le type de l'entité gérée par le dépôt
 */
public interface Repository<T> {

    /**
     * Recherche une entité par son identifiant.
     * @param id identifiant de l'entité
     * @return Optional contenant l'entité ou vide si non trouvée
     */
    Optional<T> findById(int id) throws QueuelessException;

    /**
     * Retourne toutes les entités – utilise la collection List.
     * @return liste de toutes les entités
     */
    List<T> findAll() throws QueuelessException;

    /**
     * Insère ou met à jour une entité.
     * @param entity l'entité à sauvegarder
     * @return l'entité mise à jour (avec son id généré)
     */
    T save(T entity) throws QueuelessException;

    /**
     * Supprime une entité par son identifiant.
     * @param id identifiant de l'entité à supprimer
     */
    void delete(int id) throws QueuelessException;
}
