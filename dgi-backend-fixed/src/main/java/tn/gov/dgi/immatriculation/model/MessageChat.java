package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages_chat")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
// FIX: @ToString without exclude= param + @ToString.Exclude on the field
// (cannot mix old-style exclude= with new-style @ToString.Exclude)
@ToString
@EqualsAndHashCode(of = "id")
public class MessageChat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    @ToString.Exclude
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "expediteur", nullable = false, length = 10)
    private Expediteur expediteur;

    @Column(name = "contenu", nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20)
    private SourceReponse source;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;
}
