package tn.gov.dgi.immatriculation.repository;

import tn.gov.dgi.immatriculation.model.FaqEntry;
import tn.gov.dgi.immatriculation.model.RoleCibleFaq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqEntryRepository extends JpaRepository<FaqEntry, java.util.UUID> {
    List<FaqEntry> findByActifTrueAndRoleCibleIn(List<RoleCibleFaq> roles);
}