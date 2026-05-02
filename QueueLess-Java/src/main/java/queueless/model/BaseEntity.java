package queueless.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Classe abstraite de base pour toutes les entités du domaine.
 * Démontre : classe abstraite, encapsulation, JPA @MappedSuperclass.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Constructeurs ──────────────────────────────────────────────────────
    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }

    protected BaseEntity(int id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ── Méthode abstraite – Polymorphisme ─────────────────────────────────
    /**
     * Chaque sous-classe doit fournir une représentation textuelle de l'entité.
     * Illustre le polymorphisme : comportement différent selon le type concret.
     */
    public abstract void display();

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + "]";
    }
}
