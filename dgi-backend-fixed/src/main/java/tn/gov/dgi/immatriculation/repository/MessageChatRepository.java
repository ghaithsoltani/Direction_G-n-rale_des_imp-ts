package tn.gov.dgi.immatriculation.repository;

import tn.gov.dgi.immatriculation.model.MessageChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageChatRepository extends JpaRepository<MessageChat, UUID> {
    List<MessageChat> findByConversationIdOrderByDateCreationAsc(UUID conversationId);
}