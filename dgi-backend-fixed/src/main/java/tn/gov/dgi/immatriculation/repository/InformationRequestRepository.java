package tn.gov.dgi.immatriculation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.gov.dgi.immatriculation.model.InformationRequest;
import java.util.List;
import java.util.UUID;

public interface InformationRequestRepository extends JpaRepository<InformationRequest, UUID> {
    List<InformationRequest> findByDossierIdOrderByCreatedAtDesc(UUID dossierId);
}
