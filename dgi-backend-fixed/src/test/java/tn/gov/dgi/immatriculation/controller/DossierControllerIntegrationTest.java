package tn.gov.dgi.immatriculation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tn.gov.dgi.immatriculation.dto.request.DossierCreateDTO;
import tn.gov.dgi.immatriculation.model.PersonnePhysique;
import tn.gov.dgi.immatriculation.model.TypeContribuable;
import tn.gov.dgi.immatriculation.repository.ContribuableRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration avec base H2 en mémoire (profil "test" à créer dans
 * application-test.yml avec ddl-auto: create-drop, pour ne pas dépendre de
 * Flyway/PostgreSQL réel pendant les tests). Vérifie le comportement bout
 * en bout : contrôleur -> service -> repository -> base.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DossierControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContribuableRepository contribuableRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void creerDossier_devraitRetourner201EtNumeroDossierGenere() throws Exception {
        PersonnePhysique contribuable = contribuableRepository.save(
                PersonnePhysique.builder()
                        .type(TypeContribuable.PERSONNE_PHYSIQUE)
                        .cin("87654321")
                        .email("integration.test@example.tn")
                        .nom("Test")
                        .prenom("Integration")
                        .dateNaissance(LocalDate.of(1995, 1, 1))
                        .build());

        DossierCreateDTO dto = new DossierCreateDTO(contribuable.getId());

        mockMvc.perform(post("/api/dossiers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("BROUILLON"))
                .andExpect(jsonPath("$.numeroDossier").value(org.hamcrest.Matchers.matchesPattern("DGI-\\d{4}-\\d{6}")));
    }

    @Test
    void creerDossier_devraitRetourner404SiContribuableInexistant() throws Exception {
        DossierCreateDTO dto = new DossierCreateDTO(UUID.randomUUID());

        mockMvc.perform(post("/api/dossiers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erreur").value("CONTRIBUABLE_NOT_FOUND"));
    }

    @Test
    void obtenirDossier_devraitRetourner404SiIdInexistant() throws Exception {
        mockMvc.perform(get("/api/dossiers/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}