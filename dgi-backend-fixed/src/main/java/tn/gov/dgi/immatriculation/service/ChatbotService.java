package tn.gov.dgi.immatriculation.service;

import tn.gov.dgi.immatriculation.dto.request.ChatMessageRequestDTO;
import tn.gov.dgi.immatriculation.dto.response.ChatMessageResponseDTO;
import tn.gov.dgi.immatriculation.model.Role;
import java.util.UUID;

public interface ChatbotService {
    ChatMessageResponseDTO traiterMessage(ChatMessageRequestDTO request, UUID userId, Role role);
}
