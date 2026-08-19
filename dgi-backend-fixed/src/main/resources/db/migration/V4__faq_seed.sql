-- V4__faq_seed.sql
-- Données FAQ complètes pour que le chatbot fonctionne sans LLM dès le démarrage.
-- Ces entrées couvrent les questions les plus fréquentes des contribuables et des agents.

INSERT INTO faq_entries (id, mots_cles, question, reponse, categorie, role_cible)
VALUES
-- Pour les contribuables
(gen_random_uuid(),
 'déposer,soumettre,demande,comment,démarche,immatriculation,commencer,débuter',
 'Comment déposer une demande d''immatriculation ?',
 'Pour déposer une demande : 1) Créez votre compte sur le portail DGI. 2) Renseignez vos informations personnelles (étape "Infos générales"). 3) Décrivez votre activité professionnelle. 4) Téléversez vos pièces justificatives (CIN, justificatif de domicile). 5) Effectuez la vérification faciale. 6) Soumettez votre dossier depuis l''étape "Récapitulatif".',
 'FORMULAIRE', 'CONTRIBUABLE'),

(gen_random_uuid(),
 'suivre,suivi,dossier,état,avancement,consulter,voir,où,progression',
 'Comment suivre l''état de mon dossier ?',
 'Vous pouvez demander le statut de votre dossier directement ici en écrivant "statut de mon dossier" ou en mentionnant votre numéro de dossier (format DGI-AAAA-NNNNNN). Vous pouvez également consulter votre espace personnel sur le portail DGI.',
 'SUIVI', 'CONTRIBUABLE'),

(gen_random_uuid(),
 'pièce,document,justificatif,requis,nécessaire,fournir,liste,besoin',
 'Quelles pièces dois-je fournir ?',
 'Pour une personne physique : CIN (recto/verso) + justificatif de domicile récent (moins de 3 mois). Pour une personne morale : statuts de la société + registre de commerce + CIN du représentant légal. Dans tous les cas : une photo capturée en direct pour la vérification d''identité.',
 'PIECES_JUSTIFICATIVES', 'CONTRIBUABLE'),

(gen_random_uuid(),
 'délai,combien,temps,traitement,attente,durée,réponse',
 'Combien de temps prend le traitement de mon dossier ?',
 'Le délai moyen est de 5 à 10 jours ouvrables après soumission complète. Vous recevrez une notification par email dès qu''une décision est prise. En cas de dossier incomplet, un agent peut vous contacter pour des compléments.',
 'DELAIS', 'CONTRIBUABLE'),

(gen_random_uuid(),
 'rejeté,refusé,pourquoi,rejet,refus,motif,raison',
 'Pourquoi mon dossier a-t-il été rejeté ?',
 'Le motif de rejet est précisé dans le commentaire de l''agent, visible sur votre dossier. Les causes fréquentes sont : pièce d''identité illisible ou expirée, incohérence entre les informations saisies et l''OCR, photo de vérification non concluante. Vous pouvez corriger et resoumettre un nouveau dossier.',
 'REJET', 'CONTRIBUABLE'),

(gen_random_uuid(),
 'corriger,modifier,changer,erreur,rectifier,corriger,dossier rejeté,nouveau dossier',
 'Comment corriger un dossier rejeté ?',
 'Suite à un rejet, votre dossier repasse en statut "Brouillon". Vous pouvez le corriger en : 1) Consultant le commentaire de l''agent pour identifier le problème. 2) Créant un nouveau dossier avec les informations corrigées. 3) Re-téléversant vos pièces justificatives si nécessaire. 4) Soumettant à nouveau.',
 'REJET', 'CONTRIBUABLE'),

(gen_random_uuid(),
 'format,taille,fichier,pdf,image,jpg,png,taille maximale,limite',
 'Quels formats de fichier sont acceptés ?',
 'Les formats acceptés sont : JPG, PNG et PDF. La taille maximale par fichier est de 10 Mo. Assurez-vous que vos documents sont lisibles et que les informations sont clairement visibles. Les photos floues ou trop sombres peuvent causer un rejet.',
 'PIECES_JUSTIFICATIVES', 'CONTRIBUABLE'),

(gen_random_uuid(),
 'matricule,numéro fiscal,identifiant fiscal,obtenir,recevoir',
 'Quand vais-je recevoir mon matricule fiscal ?',
 'Le matricule fiscal est attribué après validation de votre dossier par un agent DGI. Vous serez notifié par email. Le délai est généralement de 5 à 10 jours ouvrables après soumission. Pour vérifier l''avancement, demandez le statut de votre dossier ici.',
 'MATRICULE', 'CONTRIBUABLE'),

-- Pour les agents DGI
(gen_random_uuid(),
 'valider,validation,critères,conditions,dossier valide',
 'Quels sont les critères de validation d''un dossier ?',
 'Un dossier peut être validé si : 1) Toutes les pièces justificatives sont lisibles et conformes. 2) Le score de vérification faciale est supérieur au seuil configuré (75% par défaut). 3) Les données OCR correspondent aux informations saisies. 4) L''activité déclarée est cohérente avec le type de contribuable.',
 'VALIDATION', 'AGENT_DGI'),

(gen_random_uuid(),
 'rejeter,rejet,motif,agent,raison,commentaire',
 'Comment rédiger un motif de rejet clair ?',
 'Le commentaire de rejet doit être précis et actionnable pour le contribuable. Exemples : "Pièce d''identité illisible - veuillez rescanner en haute résolution", "Photo de vérification non concluante - visage partiellement caché", "Registre de commerce expiré". Évitez les formulations vagues.',
 'VALIDATION', 'AGENT_DGI');

-- Note: Si des entrées existent déjà (V3), on ignore les doublons via ON CONFLICT DO NOTHING
-- Les entrées de V3 restent présentes.
