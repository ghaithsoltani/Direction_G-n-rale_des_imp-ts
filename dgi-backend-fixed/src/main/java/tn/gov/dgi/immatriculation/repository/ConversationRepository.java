package tn.gov.dgi.immatriculation.repository;

import tn.gov.dgi.immatriculation.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByUtilisateurIdOrderByDateDerniereActiviteDesc(UUID utilisateurId);
}