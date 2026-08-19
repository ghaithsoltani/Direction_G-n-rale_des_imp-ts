-- V1__creation_schema_initial.sql
CREATE TABLE contribuables (
                               id                              UUID PRIMARY KEY,
                               type                            VARCHAR(30)  NOT NULL,
                               type_contribuable               VARCHAR(31)  NOT NULL,  -- discriminant Hibernate
                               cin                              VARCHAR(20)  UNIQUE,
                               numero_passeport                 VARCHAR(30),
                               email                            VARCHAR(150) NOT NULL,
                               telephone                        VARCHAR(20),
                               adresse_rue                      VARCHAR(200),
                               adresse_ville                    VARCHAR(100),
                               adresse_code_postal              VARCHAR(10),
                               adresse_gouvernorat               VARCHAR(100),
                               adresse_pays                      VARCHAR(100),
                               activite_code_principale          VARCHAR(20),
                               activite_libelle                  VARCHAR(200),
                               activite_secteur                  VARCHAR(100),
                               activite_date_debut               DATE,
                               activite_adresse_exercice          VARCHAR(200),
                               activite_principale               BOOLEAN,
                               date_creation                    TIMESTAMP    NOT NULL DEFAULT now(),
                               date_derniere_modification        TIMESTAMP    NOT NULL DEFAULT now()
);


CREATE TABLE utilisateurs (
                              id                   UUID PRIMARY KEY,
                              email                VARCHAR(150) NOT NULL UNIQUE,
                              mot_de_passe_hash    VARCHAR(255) NOT NULL,
                              role                 VARCHAR(20)  NOT NULL,
                              contribuable_id      UUID REFERENCES contribuables (id),
                              actif                BOOLEAN      NOT NULL DEFAULT true,
                              date_creation        TIMESTAMP    NOT NULL DEFAULT now()
);

ALTER TABLE utilisateurs
    ADD CONSTRAINT chk_role CHECK (role IN ('CONTRIBUABLE', 'AGENT_DGI', 'ADMIN'));

CREATE INDEX idx_utilisateur_contribuable ON utilisateurs (contribuable_id);


CREATE INDEX idx_contribuable_email ON contribuables (email);

CREATE TABLE personnes_physiques (
                                     id                  UUID PRIMARY KEY REFERENCES contribuables (id),
                                     nom                 VARCHAR(100) NOT NULL,
                                     prenom              VARCHAR(100) NOT NULL,
                                     date_naissance      DATE         NOT NULL,
                                     lieu_naissance      VARCHAR(100),
                                     nationalite         VARCHAR(100),
                                     genre               VARCHAR(10)
);

CREATE TABLE personnes_morales (
                                   id                          UUID PRIMARY KEY REFERENCES contribuables (id),
                                   raison_sociale              VARCHAR(200) NOT NULL,
                                   registre_commerce           VARCHAR(30) UNIQUE,
                                   forme_juridique             VARCHAR(50),
                                   date_creation_entreprise    DATE,
                                   capital_social              DOUBLE PRECISION,
                                   representant_nom            VARCHAR(100),
                                   representant_prenom         VARCHAR(100),
                                   representant_cin            VARCHAR(20),
                                   representant_qualite        VARCHAR(100)
);

CREATE TABLE dossiers_immatriculation (
                                          id                                UUID PRIMARY KEY,
                                          numero_dossier                    VARCHAR(30) NOT NULL UNIQUE,
                                          contribuable_id                   UUID NOT NULL REFERENCES contribuables (id),
                                          statut                            VARCHAR(20) NOT NULL,
                                          face_piece_reference_id           UUID,
                                          face_piece_photo_live_id          UUID,
                                          face_score_similarite             DOUBLE PRECISION,
                                          face_seuil_acceptation            DOUBLE PRECISION,
                                          face_correspondance_validee       BOOLEAN,
                                          face_date_verification            TIMESTAMP,
                                          face_message_erreur               VARCHAR(500),
                                          commentaire_agent                 VARCHAR(1000),
                                          agent_traitant_id                 UUID,
                                          date_creation                     TIMESTAMP NOT NULL DEFAULT now(),
                                          date_derniere_modification        TIMESTAMP NOT NULL DEFAULT now(),
                                          date_soumission                   TIMESTAMP
);

CREATE INDEX idx_dossier_statut ON dossiers_immatriculation (statut);
CREATE INDEX idx_dossier_contribuable ON dossiers_immatriculation (contribuable_id);

CREATE TABLE historique_statuts (
                                    dossier_id          UUID NOT NULL REFERENCES dossiers_immatriculation (id) ON DELETE CASCADE,
                                    ordre               INTEGER NOT NULL,
                                    ancien_statut       VARCHAR(20),
                                    nouveau_statut      VARCHAR(20) NOT NULL,
                                    date_changement     TIMESTAMP NOT NULL,
                                    auteur_id           UUID,
                                    commentaire         VARCHAR(500),
                                    PRIMARY KEY (dossier_id, ordre)
);

CREATE TABLE pieces_jointes (
                                id                          UUID PRIMARY KEY,
                                dossier_id                  UUID NOT NULL REFERENCES dossiers_immatriculation (id),
                                type_piece                  VARCHAR(30) NOT NULL,
                                nom_fichier_original        VARCHAR(255) NOT NULL,
                                content_type                VARCHAR(100),
                                taille_octets               BIGINT,
                                chemin_stockage              VARCHAR(500) NOT NULL,
                                ocr_nom_detecte               VARCHAR(100),
                                ocr_prenom_detecte            VARCHAR(100),
                                ocr_date_naissance_detectee    DATE,
                                ocr_numero_piece_detecte       VARCHAR(30),
                                ocr_score_confiance            DOUBLE PRECISION,
                                ocr_texte_brut_extrait          TEXT,
                                ocr_date_extraction            TIMESTAMP,
                                ocr_extraction_reussie          BOOLEAN,
                                ocr_message_erreur              VARCHAR(500),
                                date_upload                    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_piece_dossier ON pieces_jointes (dossier_id);

-- Contraintes CHECK complémentaires (gain propre au relationnel, cf. étape 2)
ALTER TABLE pieces_jointes
    ADD CONSTRAINT chk_ocr_score_confiance
        CHECK (ocr_score_confiance IS NULL OR (ocr_score_confiance >= 0 AND ocr_score_confiance <= 1));

ALTER TABLE dossiers_immatriculation
    ADD CONSTRAINT chk_face_score_similarite
        CHECK (face_score_similarite IS NULL OR (face_score_similarite >= 0 AND face_score_similarite <= 1));

ALTER TABLE contribuables
    ADD CONSTRAINT chk_type_contribuable
        CHECK (type IN ('PERSONNE_PHYSIQUE', 'PERSONNE_MORALE'));

ALTER TABLE dossiers_immatriculation
    ADD CONSTRAINT chk_statut_dossier
        CHECK (statut IN ('BROUILLON', 'SOUMIS', 'EN_TRAITEMENT', 'VALIDE', 'REJETE'));