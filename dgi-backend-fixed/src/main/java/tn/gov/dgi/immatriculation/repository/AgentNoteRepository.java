package tn.gov.dgi.immatriculation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.gov.dgi.immatriculation.model.AgentNote;
import java.util.List;
import java.util.UUID;

public interface AgentNoteRepository extends JpaRepository<AgentNote, UUID> {
    List<AgentNote> findByDossierIdOrderByCreatedAtDesc(UUID dossierId);
}
