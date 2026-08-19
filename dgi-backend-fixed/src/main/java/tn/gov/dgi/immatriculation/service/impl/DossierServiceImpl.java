package tn.gov.dgi.immatriculation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gov.dgi.immatriculation.dto.request.ChangementStatutDTO;
import tn.gov.dgi.immatriculation.dto.request.DossierCreateDTO;
import tn.gov.dgi.immatriculation.dto.response.DossierResponseDTO;
import tn.gov.dgi.immatriculation.dto.response.StatistiquesDashboardDTO;
import tn.gov.dgi.immatriculation.exception.ContribuableNotFoundException;
import tn.gov.dgi.immatriculation.exception.DossierNotFoundException;
import tn.gov.dgi.immatriculation.exception.TransitionStatutInvalideException;
import tn.gov.dgi.immatriculation.mapper.DossierMapper;
import tn.gov.dgi.immatriculation.model.*;
import tn.gov.dgi.immatriculation.repository.ContribuableRepository;
import tn.gov.dgi.immatriculation.repository.DossierImmatriculationRepository;
import tn.gov.dgi.immatriculation.service.DossierService;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DossierServiceImpl implements DossierService {

    private final DossierImmatriculationRepository dossierRepository;
    private final ContribuableRepository contribuableRepository;
    private final DossierMapper dossierMapper;

    private static final Map<StatutDossier, Set<StatutDossier>> TRANSITIONS_AUTORISEES = Map.of(
            StatutDossier.BROUILLON,               Set.of(StatutDossier.SOUMIS),
            StatutDossier.SOUMIS,                  Set.of(StatutDossier.EN_TRAITEMENT),
            StatutDossier.EN_TRAITEMENT,            Set.of(StatutDossier.VALIDE, StatutDossier.REJETE,
                    StatutDossier.EN_ATTENTE_CONTRIBUABLE),
            StatutDossier.EN_ATTENTE_CONTRIBUABLE,  Set.of(StatutDossier.EN_TRAITEMENT),
            StatutDossier.VALIDE,                  Set.of(),
            StatutDossier.REJETE,                  Set.of(StatutDossier.BROUILLON)
    );

    @Override
    public DossierResponseDTO creerBrouillon(DossierCreateDTO dto) {
        Contribuable contribuable = contribuableRepository.findById(dto.getContribuableId())
                .orElseThrow(() -> new ContribuableNotFoundException(
                        "Aucun contribuable trouvé avec l'id " + dto.getContribuableId()));

        DossierImmatriculation dossier = DossierImmatriculation.builder()
                .numeroDossier(genererNumeroDossier())
                .contribuable(contribuable)
                .statut(StatutDossier.BROUILLON)
                .build();

        DossierImmatriculation saved = dossierRepository.save(dossier);
        return dossierMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DossierResponseDTO obtenirParId(UUID dossierId) {
        return dossierMapper.toDto(chargerDossier(dossierId));
    }

    @Override
    @Transactional(readOnly = true)
    public DossierResponseDTO obtenirParNumero(String numeroDossier) {
        DossierImmatriculation dossier = dossierRepository.findByNumeroDossier(numeroDossier)
                .orElseThrow(() -> new DossierNotFoundException(
                        "Aucun dossier trouvé avec le numéro " + numeroDossier));
        return dossierMapper.toDto(dossier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DossierResponseDTO> listerParContribuable(UUID contribuableId) {
        return dossierMapper.toDtoList(
                dossierRepository.findByContribuableIdOrderByDateCreationDesc(contribuableId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DossierResponseDTO> rechercherAvecFiltres(
            StatutDossier statut, LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        // Conversion enum -> String : contournement du bug Hibernate 6 avec
        // les paramètres @Enumerated(STRING) null dans les requêtes JPQL
        String statutStr = statut != null ? statut.name() : null;
        return dossierRepository.rechercherAvecFiltres(statutStr, dateDebut, dateFin, pageable)
                .map(dossierMapper::toDto);
    }

    @Override
    public DossierResponseDTO soumettre(UUID dossierId, UUID auteurId) {
        DossierImmatriculation dossier = chargerDossier(dossierId);
        appliquerTransition(dossier, StatutDossier.SOUMIS, auteurId,
                "Soumission du dossier par le contribuable");
        dossier.setDateSoumission(LocalDateTime.now());
        return dossierMapper.toDto(dossierRepository.save(dossier));
    }

    @Override
    public DossierResponseDTO changerStatut(UUID dossierId, ChangementStatutDTO dto, UUID auteurId) {
        // FIX 8: auteurId now comes from the JWT principal (passed by the controller),
        // not from the DTO body (which allowed clients to spoof the author identity).
        DossierImmatriculation dossier = chargerDossier(dossierId);
        appliquerTransition(dossier, dto.getNouveauStatut(), auteurId, dto.getCommentaire());

        if (dto.getCommentaire() != null && dto.getNouveauStatut() == StatutDossier.REJETE) {
            dossier.setCommentaireAgent(dto.getCommentaire());
        }
        return dossierMapper.toDto(dossierRepository.save(dossier));
    }

    // ----------------- Méthodes privées -----------------

    private DossierImmatriculation chargerDossier(UUID dossierId) {
        DossierImmatriculation dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new DossierNotFoundException(
                        "Aucun dossier trouvé avec l'id " + dossierId));
        // FIX: force-initialize lazy collections inside the transaction
        // so the mapper can access them after the session closes
        if (dossier.getPiecesJointes() != null) {
            dossier.getPiecesJointes().size(); // triggers Hibernate lazy load
        }
        if (dossier.getContribuable() != null) {
            dossier.getContribuable().getEmail(); // triggers contribuable proxy init
        }
        return dossier;
    }

    private void appliquerTransition(DossierImmatriculation dossier, StatutDossier nouveauStatut,
                                     UUID auteurId, String commentaire) {
        StatutDossier ancienStatut = dossier.getStatut();
        Set<StatutDossier> transitionsPossibles = TRANSITIONS_AUTORISEES.getOrDefault(ancienStatut, Set.of());

        if (!transitionsPossibles.contains(nouveauStatut)) {
            throw new TransitionStatutInvalideException(
                    "Transition de " + ancienStatut + " vers " + nouveauStatut + " non autorisée");
        }

        dossier.setStatut(nouveauStatut);
        dossier.getHistoriqueStatuts().add(
                HistoriqueStatut.builder()
                        .ancienStatut(ancienStatut)
                        .nouveauStatut(nouveauStatut)
                        .dateChangement(LocalDateTime.now())
                        .auteurId(auteurId)
                        .commentaire(commentaire)
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public StatistiquesDashboardDTO obtenirStatistiques() {
        int anneeActuelle = Year.now().getValue();

        // Compteurs par statut (requêtes dérivées Spring Data — index sur statut recommandé)
        long brouillons    = dossierRepository.countByStatut(StatutDossier.BROUILLON);
        long soumis        = dossierRepository.countByStatut(StatutDossier.SOUMIS);
        long enTraitement  = dossierRepository.countByStatut(StatutDossier.EN_TRAITEMENT);
        long valides       = dossierRepository.countByStatut(StatutDossier.VALIDE);
        long rejetes       = dossierRepository.countByStatut(StatutDossier.REJETE);
        long total         = brouillons + soumis + enTraitement + valides + rejetes;

        // Évolution mensuelle : liste de 12 éléments (0 = Janvier … 11 = Décembre)
        List<Object[]> parMois = dossierRepository.countParMois(anneeActuelle);
        List<Long> evolution = new java.util.ArrayList<>(java.util.Collections.nCopies(12, 0L));
        for (Object[] row : parMois) {
            int mois = ((Number) row[0]).intValue(); // 1-12
            long count = ((Number) row[1]).longValue();
            if (mois >= 1 && mois <= 12) {
                evolution.set(mois - 1, count);
            }
        }

        return StatistiquesDashboardDTO.builder()
                .totalDossiers(total)
                .brouillons(brouillons)
                .soumis(soumis)
                .enTraitement(enTraitement)
                .valides(valides)
                .rejetes(rejetes)
                .evolutionMensuelle(evolution)
                .build();
    }

    private String genererNumeroDossier() {
        String annee = String.valueOf(Year.now().getValue());
        List<DossierImmatriculation> derniers = dossierRepository
                .findDerniersDossiersParAnnee(annee, PageRequest.of(0, 1, Sort.by("numeroDossier").descending()));

        int prochainNumero = 1;
        if (!derniers.isEmpty()) {
            String dernierNumero = derniers.get(0).getNumeroDossier();
            String suffixe = dernierNumero.substring(dernierNumero.lastIndexOf('-') + 1);
            prochainNumero = Integer.parseInt(suffixe) + 1;
        }
        return String.format("DGI-%s-%06d", annee, prochainNumero);
    }
}