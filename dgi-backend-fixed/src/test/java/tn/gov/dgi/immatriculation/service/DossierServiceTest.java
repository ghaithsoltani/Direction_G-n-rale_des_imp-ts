package tn.gov.dgi.immatriculation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.gov.dgi.immatriculation.dto.request.ChangementStatutDTO;
import tn.gov.dgi.immatriculation.dto.request.DossierCreateDTO;
import tn.gov.dgi.immatriculation.dto.response.DossierResponseDTO;
import tn.gov.dgi.immatriculation.exception.ContribuableNotFoundException;
import tn.gov.dgi.immatriculation.exception.DossierNotFoundException;
import tn.gov.dgi.immatriculation.exception.TransitionStatutInvalideException;
import tn.gov.dgi.immatriculation.mapper.DossierMapper;
import tn.gov.dgi.immatriculation.model.*;
import tn.gov.dgi.immatriculation.repository.ContribuableRepository;
import tn.gov.dgi.immatriculation.repository.DossierImmatriculationRepository;
import tn.gov.dgi.immatriculation.service.impl.DossierServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DossierServiceTest {

    @Mock
    private DossierImmatriculationRepository dossierRepository;

    @Mock
    private ContribuableRepository contribuableRepository;

    @Mock
    private DossierMapper dossierMapper;

    @InjectMocks
    private DossierServiceImpl dossierService;

    private UUID contribuableId;
    private UUID dossierId;
    private PersonnePhysique contribuable;

    @BeforeEach
    void setUp() {
        contribuableId = UUID.randomUUID();
        dossierId = UUID.randomUUID();
        contribuable = PersonnePhysique.builder()
                .id(contribuableId)
                .nom("Ben Ali")
                .prenom("Sami")
                .email("sami.benali@example.tn")
                .build();
    }

    @Test
    void creerBrouillon_devraitCreerUnDossierAvecNumeroSequentiel() {
        DossierCreateDTO dto = new DossierCreateDTO(contribuableId);

        when(contribuableRepository.findById(contribuableId)).thenReturn(Optional.of(contribuable));
        when(dossierRepository.findDerniersDossiersParAnnee(anyString(), any()))
                .thenReturn(List.of()); // aucun dossier existant cette année
        when(dossierRepository.save(any(DossierImmatriculation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(dossierMapper.toDto(any(DossierImmatriculation.class)))
                .thenReturn(new DossierResponseDTO());

        dossierService.creerBrouillon(dto);

        verify(dossierRepository).save(argThat(dossier ->
                dossier.getStatut() == StatutDossier.BROUILLON
                        && dossier.getNumeroDossier().matches("DGI-\\d{4}-000001")
                        && dossier.getContribuable().equals(contribuable)
        ));
    }

    @Test
    void creerBrouillon_devraitIncrementerLeNumeroSequentiel() {
        DossierCreateDTO dto = new DossierCreateDTO(contribuableId);
        DossierImmatriculation dernierDossier = DossierImmatriculation.builder()
                .numeroDossier("DGI-2026-000042")
                .build();

        when(contribuableRepository.findById(contribuableId)).thenReturn(Optional.of(contribuable));
        when(dossierRepository.findDerniersDossiersParAnnee(anyString(), any()))
                .thenReturn(List.of(dernierDossier));
        when(dossierRepository.save(any(DossierImmatriculation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(dossierMapper.toDto(any(DossierImmatriculation.class)))
                .thenReturn(new DossierResponseDTO());

        dossierService.creerBrouillon(dto);

        verify(dossierRepository).save(argThat(dossier ->
                dossier.getNumeroDossier().endsWith("000043")
        ));
    }

    @Test
    void creerBrouillon_devraitLeverExceptionSiContribuableInexistant() {
        DossierCreateDTO dto = new DossierCreateDTO(contribuableId);
        when(contribuableRepository.findById(contribuableId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dossierService.creerBrouillon(dto))
                .isInstanceOf(ContribuableNotFoundException.class)
                .hasMessageContaining(contribuableId.toString());

        verify(dossierRepository, never()).save(any());
    }

    @Test
    void soumettre_devraitPasserDeBrouillonASoumisEtHistoriser() {
        DossierImmatriculation dossier = DossierImmatriculation.builder()
                .id(dossierId)
                .statut(StatutDossier.BROUILLON)
                .historiqueStatuts(new java.util.ArrayList<>())
                .build();
        UUID auteurId = UUID.randomUUID();

        when(dossierRepository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(dossierRepository.save(any(DossierImmatriculation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(dossierMapper.toDto(any(DossierImmatriculation.class)))
                .thenReturn(new DossierResponseDTO());

        dossierService.soumettre(dossierId, auteurId);

        verify(dossierRepository).save(argThat(d ->
                d.getStatut() == StatutDossier.SOUMIS
                        && d.getDateSoumission() != null
                        && d.getHistoriqueStatuts().size() == 1
                        && d.getHistoriqueStatuts().get(0).getAncienStatut() == StatutDossier.BROUILLON
                        && d.getHistoriqueStatuts().get(0).getNouveauStatut() == StatutDossier.SOUMIS
                        && d.getHistoriqueStatuts().get(0).getAuteurId().equals(auteurId)
        ));
    }

    @Test
    void changerStatut_devraitRefuserTransitionInvalide() {
        DossierImmatriculation dossier = DossierImmatriculation.builder()
                .id(dossierId)
                .statut(StatutDossier.BROUILLON) // BROUILLON -> VALIDE est interdit
                .historiqueStatuts(new java.util.ArrayList<>())
                .build();

        when(dossierRepository.findById(dossierId)).thenReturn(Optional.of(dossier));

        ChangementStatutDTO dto = new ChangementStatutDTO(StatutDossier.VALIDE, "tentative invalide");

        assertThatThrownBy(() -> dossierService.changerStatut(dossierId, dto, UUID.randomUUID()))
                .isInstanceOf(TransitionStatutInvalideException.class)
                .hasMessageContaining("BROUILLON")
                .hasMessageContaining("VALIDE");

        verify(dossierRepository, never()).save(any());
    }

    @Test
    void changerStatut_devraitAccepterTransitionValideEtEnregistrerCommentaireSiRejet() {
        DossierImmatriculation dossier = DossierImmatriculation.builder()
                .id(dossierId)
                .statut(StatutDossier.EN_TRAITEMENT)
                .historiqueStatuts(new java.util.ArrayList<>())
                .build();
        UUID agentId = UUID.randomUUID();

        when(dossierRepository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(dossierRepository.save(any(DossierImmatriculation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(dossierMapper.toDto(any(DossierImmatriculation.class)))
                .thenReturn(new DossierResponseDTO());

        ChangementStatutDTO dto = new ChangementStatutDTO(
                StatutDossier.REJETE, "Pièce d'identité illisible");

        dossierService.changerStatut(dossierId, dto, agentId);

        verify(dossierRepository).save(argThat(d ->
                d.getStatut() == StatutDossier.REJETE
                        && "Pièce d'identité illisible".equals(d.getCommentaireAgent())
        ));
    }

    @Test
    void obtenirParId_devraitLeverExceptionSiDossierInexistant() {
        when(dossierRepository.findById(dossierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dossierService.obtenirParId(dossierId))
                .isInstanceOf(DossierNotFoundException.class);
    }
}