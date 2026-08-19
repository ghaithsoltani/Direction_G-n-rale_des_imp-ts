package tn.gov.dgi.immatriculation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gov.dgi.immatriculation.dto.response.NotificationDTO;
import tn.gov.dgi.immatriculation.model.Notification;
import tn.gov.dgi.immatriculation.repository.NotificationRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void creer(UUID userId, UUID dossierId, String title, String message, String type) {
        notificationRepository.save(Notification.builder()
                .userId(userId)
                .dossierId(dossierId)
                .title(title)
                .message(message)
                .type(type)
                .build());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> listerPourUtilisateur(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public NotificationDTO marquerLue(UUID notifId, UUID userId) {
        Notification n = notificationRepository.findById(notifId)
                .filter(notif -> notif.getUserId().equals(userId))
                .orElseThrow(() ->
                        new tn.gov.dgi.immatriculation.exception.ResourceNotFoundException("Notification introuvable"));
        n.setLu(true);
        return toDto(notificationRepository.save(n));
    }

    public int marquerToutesLues(UUID userId) {
        return notificationRepository.marquerToutesLues(userId);
    }

    private NotificationDTO toDto(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId()).dossierId(n.getDossierId())
                .title(n.getTitle()).message(n.getMessage())
                .type(n.getType()).lu(n.isLu()).createdAt(n.getCreatedAt())
                .build();
    }
}