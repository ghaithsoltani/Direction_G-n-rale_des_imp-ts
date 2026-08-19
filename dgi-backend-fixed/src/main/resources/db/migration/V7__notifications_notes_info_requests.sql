-- V7: Notifications, agent notes, information requests, dossier enhancements
-- ============================================================

-- 1. Add EN_ATTENTE_CONTRIBUABLE to dossier workflow (StatutDossier enum — no DB change needed, stored as STRING)
-- But we add priority, deadline, assigned_at to dossiers_immatriculation
ALTER TABLE dossiers_immatriculation
    ADD COLUMN IF NOT EXISTS priorite          VARCHAR(10)  DEFAULT 'NORMALE',
    ADD COLUMN IF NOT EXISTS deadline          TIMESTAMP,
    ADD COLUMN IF NOT EXISTS assigned_at       TIMESTAMP;

-- 2. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    dossier_id  UUID         REFERENCES dossiers_immatriculation(id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    message     TEXT         NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    lu          BOOLEAN      NOT NULL DEFAULT false,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_notif_user     ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notif_dossier  ON notifications(dossier_id);
CREATE INDEX IF NOT EXISTS idx_notif_unread   ON notifications(user_id, lu) WHERE lu = false;

-- 3. Agent internal notes
CREATE TABLE IF NOT EXISTS agent_notes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dossier_id  UUID         NOT NULL REFERENCES dossiers_immatriculation(id) ON DELETE CASCADE,
    agent_id    UUID         NOT NULL,
    content     TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_notes_dossier ON agent_notes(dossier_id);

-- 4. Information requests
CREATE TABLE IF NOT EXISTS information_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dossier_id      UUID         NOT NULL REFERENCES dossiers_immatriculation(id) ON DELETE CASCADE,
    requested_by    UUID         NOT NULL,
    message         TEXT         NOT NULL,
    statut          VARCHAR(20)  NOT NULL DEFAULT 'EN_ATTENTE',
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    responded_at    TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_inforeq_dossier ON information_requests(dossier_id);

-- 5. User profile fields
ALTER TABLE utilisateurs
    ADD COLUMN IF NOT EXISTS prenom           VARCHAR(100),
    ADD COLUMN IF NOT EXISTS nom              VARCHAR(100),
    ADD COLUMN IF NOT EXISTS telephone        VARCHAR(30),
    ADD COLUMN IF NOT EXISTS langue_preferee  VARCHAR(5)   DEFAULT 'fr',
    ADD COLUMN IF NOT EXISTS notif_email      BOOLEAN      DEFAULT true,
    ADD COLUMN IF NOT EXISTS notif_app        BOOLEAN      DEFAULT true;
