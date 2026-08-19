-- V8__utilisateur_notif_defaults.sql
--
-- BUG-DB-1 FIX (corollaire de BV7 FIX) :
--   La migration V7 ajoutait notif_email, notif_app et langue_preferee avec
--   DEFAULT mais SANS NOT NULL. Hibernate @Builder.Default = true sur un
--   boolean primitif lève une NullPointerException quand la colonne contient
--   NULL (INSERT de register-agent qui ne fournit pas ces colonnes).
--
--   Ce script :
--   1. Met à jour les lignes existantes qui auraient pu recevoir NULL
--   2. Applique NOT NULL + DEFAULT propres sur les trois colonnes

-- 1. Remplir les NULL existants avant d'ajouter la contrainte NOT NULL
UPDATE utilisateurs SET notif_email     = true  WHERE notif_email     IS NULL;
UPDATE utilisateurs SET notif_app       = true  WHERE notif_app       IS NULL;
UPDATE utilisateurs SET langue_preferee = 'fr'  WHERE langue_preferee IS NULL;

-- 2. Appliquer NOT NULL + DEFAULT
ALTER TABLE utilisateurs
    ALTER COLUMN notif_email     SET NOT NULL,
    ALTER COLUMN notif_email     SET DEFAULT true,
    ALTER COLUMN notif_app       SET NOT NULL,
    ALTER COLUMN notif_app       SET DEFAULT true,
    ALTER COLUMN langue_preferee SET NOT NULL,
    ALTER COLUMN langue_preferee SET DEFAULT 'fr';
