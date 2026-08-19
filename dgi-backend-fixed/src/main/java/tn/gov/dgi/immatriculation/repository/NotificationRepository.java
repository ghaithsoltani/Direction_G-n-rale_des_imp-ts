package tn.gov.dgi.immatriculation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tn.gov.dgi.immatriculation.model.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    long countByUserIdAndLuFalse(UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.lu = true WHERE n.userId = :userId AND n.lu = false")
    int marquerToutesLues(UUID userId);
}
