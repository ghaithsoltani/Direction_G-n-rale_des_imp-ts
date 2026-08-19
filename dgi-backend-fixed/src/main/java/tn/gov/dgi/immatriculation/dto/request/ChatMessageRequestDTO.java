package tn.gov.dgi.immatriculation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDTO {

    @NotBlank(message = "Le message ne peut pas être vide")
    private String message;

    /**
     * null pour démarrer une nouvelle conversation.
     * Accepte une chaîne UUID (ex: "550e8400-e29b-41d4-a716-446655440000").
     * Jackson deserialise automatiquement String -> UUID via le constructeur UUID.fromString().
     * FIX: l'ancienne version échouait quand le frontend envoyait null (JSON null) car
     * Jackson ne peut pas convertir null en UUID avec certaines configurations.
     * On reçoit maintenant une String et on convertit manuellement.
     */
    private String conversationId;

    /**
     * Retourne le conversationId sous forme de UUID, ou null si absent/invalide.
     */
    public UUID getConversationIdAsUUID() {
        if (conversationId == null || conversationId.isBlank()) return null;
        try {
            return UUID.fromString(conversationId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
