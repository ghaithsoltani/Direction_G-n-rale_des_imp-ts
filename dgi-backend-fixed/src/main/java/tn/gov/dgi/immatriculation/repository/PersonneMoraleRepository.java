package tn.gov.dgi.immatriculation.repository;

import tn.gov.dgi.immatriculation.model.PersonneMorale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PersonneMoraleRepository extends JpaRepository<PersonneMorale, UUID> {

    Optional<PersonneMorale> findByRegistreCommerce(String registreCommerce);

    boolean existsByRegistreCommerce(String registreCommerce);

    Optional<PersonneMorale> findByRaisonSocialeIgnoreCase(String raisonSociale);
}