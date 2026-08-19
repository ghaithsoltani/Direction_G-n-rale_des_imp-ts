CREATE TABLE faq_entries (
                             id              UUID PRIMARY KEY,
                             mots_cles       TEXT NOT NULL,        -- mots-clés séparés par virgules
                             question        VARCHAR(500) NOT NULL,
                             reponse         TEXT NOT NULL,
                             categorie       VARCHAR(50),          -- ex: FORMULAIRE, PIECES_JUSTIFICATIVES, DELAIS
                             role_cible      VARCHAR(20) NOT NULL DEFAULT 'TOUS', -- TOUS, CONTRIBUABLE, AGENT_DGI
                             actif           BOOLEAN NOT NULL DEFAULT true,
                             date_creation   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE conversations (
                               id              UUID PRIMARY KEY,
                               utilisateur_id  UUID NOT NULL REFERENCES utilisateurs (id),
                               role            VARCHAR(20) NOT NULL,
                               date_creation   TIMESTAMP NOT NULL DEFAULT now(),
                               date_derniere_activite TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE messages_chat (
                               id              UUID PRIMARY KEY,
                               conversation_id UUID NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
                               expediteur      VARCHAR(10) NOT NULL,  -- USER, ASSISTANT
                               contenu         TEXT NOT NULL,
                               source          VARCHAR(20),           -- FAQ, LLM, DOSSIER_STATUS, ERREUR
                               date_creation   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_conversation_utilisateur ON conversations (utilisateur_id);
CREATE INDEX idx_message_conversation ON messages_chat (conversation_id);

-- Quelques FAQ de démarrage
INSERT INTO faq_entries (id, mots_cles, question, reponse, categorie, role_cible) VALUES
                                                                                      (gen_random_uuid(), 'pièce,document,justificatif,requis,nécessaire',
                                                                                       'Quelles pièces dois-je fournir ?',
                                                                                       'Pour une personne physique : CIN (recto/verso) et justificatif de domicile. Pour une personne morale : statuts de la société et registre de commerce. Une photo capturée en direct est également requise pour la vérification d''identité.',
                                                                                       'PIECES_JUSTIFICATIVES', 'CONTRIBUABLE'),
                                                                                      (gen_random_uuid(), 'délai,combien,temps,traitement,attente',
                                                                                       'Combien de temps prend le traitement de mon dossier ?',
                                                                                       'Le délai moyen de traitement est de 5 à 10 jours ouvrables après soumission complète du dossier. Vous pouvez suivre l''état d''avancement à tout moment en demandant le statut de votre dossier ici.',
                                                                                       'DELAIS', 'CONTRIBUABLE'),
                                                                                      (gen_random_uuid(), 'rejeté,refusé,pourquoi,rejet',
                                                                                       'Pourquoi mon dossier a-t-il été rejeté ?',
                                                                                       'Le motif de rejet est indiqué dans le commentaire de l''agent visible sur votre dossier. Les causes fréquentes : pièce illisible, informations incohérentes avec l''OCR, ou photo de vérification non concluante. Vous pouvez soumettre un nouveau dossier corrigé.',
                                                                                       'REJET', 'CONTRIBUABLE');