package tn.gov.dgi.immatriculation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.gov.dgi.immatriculation.dto.request.ActiviteDTO;
import tn.gov.dgi.immatriculation.dto.request.ContribuableCreateDTO;
import tn.gov.dgi.immatriculation.exception.DocumentInvalideException;
import tn.gov.dgi.immatriculation.mapper.ContribuableMapper;
import tn.gov.dgi.immatriculation.model.PersonnePhysique;
import tn.gov.dgi.immatriculation.model.TypeContribuable;
import tn.gov.dgi.immatriculation.repository.ContribuableRepository;
import tn.gov.dgi.immatriculation.repository.PersonneMoraleRepository;
import tn.gov.dgi.immatriculation.service.impl.ContribuableServiceImpl;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContribuableServiceTest {

    @Mock
    private ContribuableRepository contribuableRepository;

    @Mock
    private PersonneMoraleRepository personneMoraleRepository;

    @Mock
    private ContribuableMapper contribuableMapper;

    @InjectMocks
    private ContribuableServiceImpl contribuableService;

    private ContribuableCreateDTO.PersonnePhysiqueCreateDTO creerDtoValide() {
        ContribuableCreateDTO.PersonnePhysiqueCreateDTO dto = new ContribuableCreateDTO.PersonnePhysiqueCreateDTO();
        dto.setType(TypeContribuable.PERSONNE_PHYSIQUE);
        dto.setCin("12345678");
        dto.setEmail("test@example.tn");
        dto.setNom("Trabelsi");
        dto.setPrenom("Amine");
        dto.setDateNaissance(LocalDate.of(1990, 5, 12));
        return dto;
    }

    @Test
    void creer_devraitLeverExceptionSiCinDejaUtilise() {
        ContribuableCreateDTO.PersonnePhysiqueCreateDTO dto = creerDtoValide();
        when(contribuableRepository.existsByCin("12345678")).thenReturn(true);

        assertThatThrownBy(() -> contribuableService.creer(dto))
                .isInstanceOf(DocumentInvalideException.class)
                .hasMessageContaining("12345678");

        verify(contribuableRepository, never()).save(any());
    }

    @Test
    void creer_devraitLeverExceptionSiEmailDejaUtilise() {
        ContribuableCreateDTO.PersonnePhysiqueCreateDTO dto = creerDtoValide();
        when(contribuableRepository.existsByCin("12345678")).thenReturn(false);
        when(contribuableRepository.existsByEmail("test@example.tn")).thenReturn(true);

        assertThatThrownBy(() -> contribuableService.creer(dto))
                .isInstanceOf(DocumentInvalideException.class)
                .hasMessageContaining("test@example.tn");
    }

    @Test
    void creer_devraitSauvegarderSiDonneesValidesEtUniques() {
        ContribuableCreateDTO.PersonnePhysiqueCreateDTO dto = creerDtoValide();
        PersonnePhysique entite = new PersonnePhysique();

        when(contribuableRepository.existsByCin("12345678")).thenReturn(false);
        when(contribuableRepository.existsByEmail("test@example.tn")).thenReturn(false);
        when(contribuableMapper.toEntity(dto)).thenReturn(entite);
        when(contribuableRepository.save(entite)).thenReturn(entite);

        contribuableService.creer(dto);

        verify(contribuableRepository).save(entite);
    }
}