-- V6__password_reset.sql
-- Ajoute les colonnes de réinitialisation de mot de passe sur la table utilisateurs.
-- (renommé depuis V5 — conflit résolu avec V5__faq_fix.sql)

ALTER TABLE utilisateurs
    ADD COLUMN IF NOT EXISTS reset_token       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reset_token_expiry TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_utilisateurs_reset_token
    ON utilisateurs (reset_token)
    WHERE reset_token IS NOT NULL;