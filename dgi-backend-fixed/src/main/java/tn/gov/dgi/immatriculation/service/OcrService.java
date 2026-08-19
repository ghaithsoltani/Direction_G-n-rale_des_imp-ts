package tn.gov.dgi.immatriculation.service;

import org.springframework.web.multipart.MultipartFile;
import tn.gov.dgi.immatriculation.model.ResultatOcr;
import tn.gov.dgi.immatriculation.model.TypePieceJointe;

public interface OcrService {

    /**
     * Extrait les champs structurés (nom, prénom, date de naissance,
     * numéro de pièce) depuis une image ou un PDF scanné. Le typePiece
     * conditionne le pattern de parsing appliqué (le format d'une CIN
     * tunisienne diffère de celui d'un passeport).
     */
    ResultatOcr extraireDonnees(MultipartFile fichier, TypePieceJointe typePiece);
}