package tn.gov.dgi.immatriculation.service;

import tn.gov.dgi.immatriculation.dto.request.ContribuableCreateDTO;
import tn.gov.dgi.immatriculation.dto.response.ContribuableResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContribuableService {

    ContribuableResponseDTO creer(ContribuableCreateDTO dto);

    ContribuableResponseDTO obtenirParId(UUID id);

    ContribuableResponseDTO obtenirParCin(String cin);

    Page<ContribuableResponseDTO> lister(Pageable pageable);
}