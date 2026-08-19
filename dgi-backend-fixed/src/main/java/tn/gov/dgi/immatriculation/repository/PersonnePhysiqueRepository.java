package tn.gov.dgi.immatriculation.repository;

import tn.gov.dgi.immatriculation.model.PersonnePhysique;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository dédié au sous-type PersonnePhysique. Utile lorsque le service
 * a besoin de filtrer/rechercher sur des champs spécifiques à ce sous-type
 * (nom, prénom) que ContribuableRepository ne peut pas exposer (il ne
 * connaît que les champs communs de la classe de base).
 */
public interface PersonnePhysiqueRepository extends JpaRepository<PersonnePhysique, UUID> {

    List<PersonnePhysique> findByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);

    List<PersonnePhysique> findByNomContainingIgnoreCase(String nomPartiel);
}