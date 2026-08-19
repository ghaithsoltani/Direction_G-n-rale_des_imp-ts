-- V2__index_optimisations.sql
-- Ajout d'index supplémentaires pour les performances de recherche
-- (migration V2 requise pour assurer la continuité de version Flyway entre V1 et V3)

-- Index sur le numéro de dossier pour les recherches chatbot
CREATE INDEX IF NOT EXISTS idx_dossier_numero ON dossiers_immatriculation (numero_dossier);

-- Index sur la date de soumission pour le filtre date dans la liste agent
CREATE INDEX IF NOT EXISTS idx_dossier_date_soumission ON dossiers_immatriculation (date_soumission);

-- Index sur l'email du contribuable pour les recherches rapides
CREATE INDEX IF NOT EXISTS idx_contribuable_cin ON contribuables (cin);

-- Index sur agent_traitant_id pour la liste des dossiers par agent
CREATE INDEX IF NOT EXISTS idx_dossier_agent ON dossiers_immatriculation (agent_traitant_id);
