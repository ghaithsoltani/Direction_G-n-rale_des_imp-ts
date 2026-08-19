package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueStatut {

    @Enumerated(EnumType.STRING)
    @Column(name = "ancien_statut", length = 20)
    private StatutDossier ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_statut", nullable = false, length = 20)
    private StatutDossier nouveauStatut;

    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;

    @Column(name = "auteur_id")
    private UUID auteurId;

    @Column(name = "commentaire", length = 500)
    private String commentaire;
}