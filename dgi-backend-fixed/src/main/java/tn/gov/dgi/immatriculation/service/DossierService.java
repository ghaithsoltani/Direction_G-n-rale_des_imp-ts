package tn.gov.dgi.immatriculation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.gov.dgi.immatriculation.dto.request.ChangementStatutDTO;
import tn.gov.dgi.immatriculation.dto.request.DossierCreateDTO;
import tn.gov.dgi.immatriculation.dto.response.DossierResponseDTO;
import tn.gov.dgi.immatriculation.dto.response.StatistiquesDashboardDTO;
import tn.gov.dgi.immatriculation.model.StatutDossier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DossierService {

    DossierResponseDTO creerBrouillon(DossierCreateDTO dto);

    DossierResponseDTO obtenirParId(UUID dossierId);

    DossierResponseDTO obtenirParNumero(String numeroDossier);

    List<DossierResponseDTO> listerParContribuable(UUID contribuableId);

    Page<DossierResponseDTO> rechercherAvecFiltres(
            StatutDossier statut, LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable);

    DossierResponseDTO soumettre(UUID dossierId, UUID auteurId);

    DossierResponseDTO changerStatut(UUID dossierId, ChangementStatutDTO dto, UUID auteurId);

    /** Retourne les statistiques globales pour le dashboard agent DGI. */
    StatistiquesDashboardDTO obtenirStatistiques();
}
