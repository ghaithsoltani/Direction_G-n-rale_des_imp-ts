package tn.gov.dgi.immatriculation.repository;

import tn.gov.dgi.immatriculation.model.Contribuable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository sur la classe de base Contribuable. Grâce à l'héritage JOINED,
 * une requête sur cette interface renvoie indifféremment des instances de
 * PersonnePhysique ou PersonneMorale (Hibernate résout le type réel via le
 * discriminant + jointure automatique).
 *
 * Les vérifications d'unicité (CIN, registre de commerce) sont exposées ici
 * plutôt que dupliquées dans les repositories filles, car elles doivent
 * être vérifiées tous types de contribuables confondus (un CIN ne doit pas
 * pouvoir être réutilisé même entre deux enregistrements de types différents
 * si jamais un tel cas de figure existait).
 */
public interface ContribuableRepository extends JpaRepository<Contribuable, UUID> {

    Optional<Contribuable> findByCin(String cin);

    boolean existsByCin(String cin);

    Optional<Contribuable> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Recherche insensible à la casse, utile pour l'écran agent DGI
     * (recherche rapide par email/CIN sans exiger une saisie exacte).
     */
    @Query("SELECT c FROM Contribuable c WHERE LOWER(c.cin) = LOWER(:cin)")
    Optional<Contribuable> findByCinIgnoreCase(@Param("cin") String cin);
}