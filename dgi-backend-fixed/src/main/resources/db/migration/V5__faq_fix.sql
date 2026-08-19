-- V5__faq_fix.sql
-- Garantit la présence des entrées FAQ critiques pour les boutons rapides du chatbot.
-- Utilise une insertion conditionnelle (INSERT ... WHERE NOT EXISTS) pour éviter
-- les doublons si V4 a déjà inséré ces entrées.

-- FAQ : "Où suivre mon dossier ?" / "Comment suivre l'état de mon dossier ?"
INSERT INTO faq_entries (id, mots_cles, question, reponse, categorie, role_cible, actif)
SELECT gen_random_uuid(),
       'suivre,suivi,dossier,état,avancement,consulter,voir,où,progression',
       'Comment suivre l''état de mon dossier ?',
       'Vous pouvez demander le statut de votre dossier directement ici en écrivant "statut de mon dossier" ou en mentionnant votre numéro de dossier (format DGI-AAAA-NNNNNN). Vous pouvez également consulter votre espace personnel sur le portail DGI.',
       'SUIVI', 'CONTRIBUABLE', true
WHERE NOT EXISTS (
    SELECT 1 FROM faq_entries WHERE mots_cles LIKE '%suivre%' AND role_cible = 'CONTRIBUABLE'
);

-- FAQ : "Comment déposer une demande ?" / "Comment commencer une demande d'immatriculation ?"
INSERT INTO faq_entries (id, mots_cles, question, reponse, categorie, role_cible, actif)
SELECT gen_random_uuid(),
       'déposer,soumettre,demande,comment,démarche,immatriculation,commencer,débuter',
       'Comment déposer une demande d''immatriculation ?',
       'Pour déposer une demande : 1) Créez votre compte sur le portail DGI. 2) Renseignez vos informations personnelles (étape "Infos générales"). 3) Décrivez votre activité professionnelle. 4) Téléversez vos pièces justificatives (CIN, justificatif de domicile). 5) Effectuez la vérification faciale. 6) Soumettez votre dossier depuis l''étape "Récapitulatif".',
       'FORMULAIRE', 'CONTRIBUABLE', true
WHERE NOT EXISTS (
    SELECT 1 FROM faq_entries WHERE mots_cles LIKE '%déposer%' AND role_cible = 'CONTRIBUABLE'
);

-- FAQ : Délais (renforcement des mots-clés si entrée V3 insuffisante)
INSERT INTO faq_entries (id, mots_cles, question, reponse, categorie, role_cible, actif)
SELECT gen_random_uuid(),
       'délai,combien,temps,traitement,attente,durée,réponse,jours',
       'Combien de temps prend le traitement de mon dossier ?',
       'Le délai moyen est de 5 à 10 jours ouvrables après soumission complète. Vous recevrez une notification par email dès qu''une décision est prise. En cas de dossier incomplet, un agent peut vous contacter pour des compléments.',
       'DELAIS', 'CONTRIBUABLE', true
WHERE NOT EXISTS (
    SELECT 1 FROM faq_entries WHERE mots_cles LIKE '%délai%' AND mots_cles LIKE '%durée%' AND role_cible = 'CONTRIBUABLE'
);

-- FAQ : Pièces justificatives (renforcement)
INSERT INTO faq_entries (id, mots_cles, question, reponse, categorie, role_cible, actif)
SELECT gen_random_uuid(),
       'pièce,document,justificatif,requis,nécessaire,fournir,liste,besoin,cin,passeport',
       'Quelles pièces dois-je fournir ?',
       'Pour une personne physique : CIN (recto/verso) + justificatif de domicile récent (moins de 3 mois). Pour une personne morale : statuts de la société + registre de commerce + CIN du représentant légal. Dans tous les cas : une photo capturée en direct pour la vérification d''identité.',
       'PIECES_JUSTIFICATIVES', 'CONTRIBUABLE', true
WHERE NOT EXISTS (
    SELECT 1 FROM faq_entries WHERE mots_cles LIKE '%fournir%' AND role_cible = 'CONTRIBUABLE'
);

-- FAQ agents DGI : validation
INSERT INTO faq_entries (id, mots_cles, question, reponse, categorie, role_cible, actif)
SELECT gen_random_uuid(),
       'valider,validation,critères,conditions,dossier valide,approuver',
       'Quels sont les critères de validation d''un dossier ?',
       'Un dossier peut être validé si : 1) Toutes les pièces justificatives sont lisibles et conformes. 2) Le score de vérification faciale est supérieur au seuil configuré (75% par défaut). 3) Les données OCR correspondent aux informations saisies. 4) L''activité déclarée est cohérente avec le type de contribuable.',
       'VALIDATION', 'AGENT_DGI', true
WHERE NOT EXISTS (
    SELECT 1 FROM faq_entries WHERE mots_cles LIKE '%valider%' AND role_cible = 'AGENT_DGI'
);
