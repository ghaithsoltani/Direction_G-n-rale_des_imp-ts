package tn.gov.dgi.immatriculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.gov.dgi.immatriculation.dto.request.ChangePasswordDTO;
import tn.gov.dgi.immatriculation.dto.request.UpdateProfileDTO;
import tn.gov.dgi.immatriculation.dto.response.UserProfileDTO;
import tn.gov.dgi.immatriculation.exception.DgiException;
import tn.gov.dgi.immatriculation.model.Utilisateur;
import tn.gov.dgi.immatriculation.repository.UtilisateurRepository;
import tn.gov.dgi.immatriculation.security.UtilisateurPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Profil utilisateur")
public class UserController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    @Operation(summary = "Obtenir mon profil")
    public ResponseEntity<UserProfileDTO> monProfil(
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        Utilisateur u = charger(principal);
        return ResponseEntity.ok(toDto(u));
    }

    @PutMapping("/me")
    @Operation(summary = "Mettre à jour mon profil")
    public ResponseEntity<UserProfileDTO> mettreAJour(
            @Valid @RequestBody UpdateProfileDTO dto,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        Utilisateur u = charger(principal);
        if (dto.getPrenom()         != null) u.setPrenom(dto.getPrenom());
        if (dto.getNom()            != null) u.setNom(dto.getNom());
        if (dto.getTelephone()      != null) u.setTelephone(dto.getTelephone());
        if (dto.getLanguePreferee() != null) u.setLanguePreferee(dto.getLanguePreferee());
        if (dto.getNotifEmail()     != null) u.setNotifEmail(dto.getNotifEmail());
        if (dto.getNotifApp()       != null) u.setNotifApp(dto.getNotifApp());
        return ResponseEntity.ok(toDto(utilisateurRepository.save(u)));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Changer mon mot de passe")
    public ResponseEntity<Map<String, String>> changerMotDePasse(
            @Valid @RequestBody ChangePasswordDTO dto,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        Utilisateur u = charger(principal);
        if (!passwordEncoder.matches(dto.getAncienMotDePasse(), u.getMotDePasseHash())) {
            throw new tn.gov.dgi.immatriculation.exception.MotDePasseIncorrectException("L'ancien mot de passe est incorrect");
        }
        u.setMotDePasseHash(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        utilisateurRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
    }

    private Utilisateur charger(UtilisateurPrincipal principal) {
        return utilisateurRepository.findById(principal.getId())
                .orElseThrow(() -> new tn.gov.dgi.immatriculation.exception.ResourceNotFoundException("Utilisateur introuvable"));
    }

    private UserProfileDTO toDto(Utilisateur u) {
        return UserProfileDTO.builder()
                .id(u.getId()).email(u.getEmail()).role(u.getRole())
                .prenom(u.getPrenom()).nom(u.getNom()).telephone(u.getTelephone())
                .languePreferee(u.getLanguePreferee())
                .notifEmail(u.isNotifEmail()).notifApp(u.isNotifApp())
                .contribuableId(u.getContribuableId())
                .build();
    }
}