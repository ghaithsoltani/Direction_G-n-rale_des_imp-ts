package tn.gov.dgi.immatriculation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gov.dgi.immatriculation.dto.request.ContribuableCreateDTO;
import tn.gov.dgi.immatriculation.dto.response.ContribuableResponseDTO;
import tn.gov.dgi.immatriculation.exception.ContribuableNotFoundException;
import tn.gov.dgi.immatriculation.exception.DocumentInvalideException;
import tn.gov.dgi.immatriculation.mapper.ContribuableMapper;
import tn.gov.dgi.immatriculation.model.Contribuable;
import tn.gov.dgi.immatriculation.model.PersonneMorale;
import tn.gov.dgi.immatriculation.model.PersonnePhysique;
import tn.gov.dgi.immatriculation.repository.ContribuableRepository;
import tn.gov.dgi.immatriculation.repository.PersonneMoraleRepository;
import tn.gov.dgi.immatriculation.service.ContribuableService;

import java.util.UUID;

/**
 * Toute la logique métier de validation vit ici, jamais dans le contrôleur
 * (architecture en couches strictement respectée).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ContribuableServiceImpl implements ContribuableService {

    private final ContribuableRepository contribuableRepository;
    private final PersonneMoraleRepository personneMoraleRepository;
    private final ContribuableMapper contribuableMapper;

    @Override
    public ContribuableResponseDTO creer(ContribuableCreateDTO dto) {
        verifierUnicite(dto);

        Contribuable entite = switch (dto) {
            case ContribuableCreateDTO.PersonnePhysiqueCreateDTO pp ->
                    contribuableMapper.toEntity(pp);
            case ContribuableCreateDTO.PersonneMoraleCreateDTO pm ->
                    contribuableMapper.toEntity(pm);
            default -> throw new DocumentInvalideException(
                    "Type de contribuable non reconnu : " + dto.getClass());
        };

        Contribuable saved = contribuableRepository.save(entite);
        return contribuableMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContribuableResponseDTO obtenirParId(UUID id) {
        Contribuable entite = contribuableRepository.findById(id)
                .orElseThrow(() -> new ContribuableNotFoundException(
                        "Aucun contribuable trouvé avec l'id " + id));
        return contribuableMapper.toDto(entite);
    }

    @Override
    @Transactional(readOnly = true)
    public ContribuableResponseDTO obtenirParCin(String cin) {
        Contribuable entite = contribuableRepository.findByCin(cin)
                .orElseThrow(() -> new ContribuableNotFoundException(
                        "Aucun contribuable trouvé avec le CIN " + cin));
        return contribuableMapper.toDto(entite);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContribuableResponseDTO> lister(Pageable pageable) {
        return contribuableRepository.findAll(pageable)
                .map(contribuableMapper::toDto);
    }

    /**
     * Validation métier centralisée : unicité du CIN (tous types confondus)
     * et unicité du registre de commerce (personnes morales uniquement).
     * Ces règles ne peuvent pas être exprimées uniquement par la contrainte
     * UNIQUE en base, car on veut ici un message d'erreur métier clair
     * (400 avec message explicite) plutôt qu'une exception SQL brute
     * remontant en 500.
     */
    private void verifierUnicite(ContribuableCreateDTO dto) {
        if (dto.getCin() != null && contribuableRepository.existsByCin(dto.getCin())) {
            throw new DocumentInvalideException(
                    "Un contribuable avec le CIN " + dto.getCin() + " existe déjà");
        }
        if (contribuableRepository.existsByEmail(dto.getEmail())) {
            throw new DocumentInvalideException(
                    "Un contribuable avec l'email " + dto.getEmail() + " existe déjà");
        }
        if (dto instanceof ContribuableCreateDTO.PersonneMoraleCreateDTO pm
                && personneMoraleRepository.existsByRegistreCommerce(pm.getRegistreCommerce())) {
            throw new DocumentInvalideException(
                    "Une personne morale avec le registre de commerce "
                            + pm.getRegistreCommerce() + " existe déjà");
        }
    }
}