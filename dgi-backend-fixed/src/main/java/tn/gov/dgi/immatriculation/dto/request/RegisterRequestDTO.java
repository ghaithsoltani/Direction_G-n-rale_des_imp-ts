package tn.gov.dgi.immatriculation.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Inscription d'un compte CONTRIBUABLE, liée à un contribuable métier déjà
 * créé via POST /api/contribuables (le flux reste en deux temps : créer la
 * fiche contribuable, puis créer le compte de connexion associé).
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class RegisterRequestDTO {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String motDePasse;

    // @NotNull
    private UUID contribuableId;
}