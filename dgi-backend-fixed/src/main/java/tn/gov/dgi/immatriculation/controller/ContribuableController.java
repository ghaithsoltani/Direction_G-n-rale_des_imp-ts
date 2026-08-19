package tn.gov.dgi.immatriculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.gov.dgi.immatriculation.dto.request.ContribuableCreateDTO;
import tn.gov.dgi.immatriculation.dto.response.ContribuableResponseDTO;
import tn.gov.dgi.immatriculation.service.ContribuableService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/contribuables")
@RequiredArgsConstructor
@Tag(name = "Contribuables", description = "Gestion des contribuables (personnes physiques et morales)")
public class ContribuableController {

    private final ContribuableService contribuableService;

    @PostMapping
    @Operation(summary = "Créer un nouveau contribuable (personne physique ou morale)")
    public ResponseEntity<ContribuableResponseDTO> creer(@Valid @RequestBody ContribuableCreateDTO dto) {
        ContribuableResponseDTO cree = contribuableService.creer(dto);
        return ResponseEntity
                .created(URI.create("/api/contribuables/" + cree.getId()))
                .body(cree);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un contribuable par son identifiant")
    public ResponseEntity<ContribuableResponseDTO> obtenirParId(@PathVariable UUID id) {
        return ResponseEntity.ok(contribuableService.obtenirParId(id));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher un contribuable par CIN")
    public ResponseEntity<ContribuableResponseDTO> obtenirParCin(@RequestParam String cin) {
        return ResponseEntity.ok(contribuableService.obtenirParCin(cin));
    }

    @GetMapping
    @Operation(summary = "Lister les contribuables (paginé)")
    public ResponseEntity<Page<ContribuableResponseDTO>> lister(Pageable pageable) {
        return ResponseEntity.ok(contribuableService.lister(pageable));
    }
}