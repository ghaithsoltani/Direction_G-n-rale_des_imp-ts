package tn.gov.dgi.immatriculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.gov.dgi.immatriculation.dto.response.CinOcrResponseDTO;
import tn.gov.dgi.immatriculation.model.TypePieceJointe;
import tn.gov.dgi.immatriculation.service.impl.OcrServiceImpl;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR", description = "Extraction automatique des données des pièces d'identité tunisiennes")
public class OcrController {

    private final OcrServiceImpl ocrService;

    /**
     * POST /api/ocr/extract?typePiece=CIN&languages=ara+fra
     *
     * Accepted formats : JPG, JPEG, PNG, PDF
     * Max size         : 5 MB
     * Languages        : ara, fra, ara+fra (default)
     */
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Extraire les données d'une CIN ou d'un passeport tunisien",
        description = """
            Envoyer l'image ou le PDF de la pièce d'identité.
            
            Pour une CIN tunisienne, le système détecte automatiquement :
            - اللقب → nomDetecte
            - الاسم → prenomDetecte
            - تاريخ الولادة → dateNaissanceDetectee (ISO YYYY-MM-DD)
            - رقم بطاقة التعريف → numeroPieceDetecte (8 chiffres)
            - مكانها → lieuNaissance (optionnel)
            
            Les chiffres arabes (٠١٢٣٤٥٦٧٨٩) sont convertis automatiquement.
            Les noms arabes sont préservés sans translittération.
            Les champs non détectés retournent null (jamais de valeur inventée).
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Extraction réussie",
            content = @Content(schema = @Schema(implementation = CinOcrResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Fichier invalide (type, taille, corrompu)"),
        @ApiResponse(responseCode = "422", description = "Extraction impossible (image illisible)")
    })
    public ResponseEntity<CinOcrResponseDTO> extraire(
            @Parameter(description = "Image ou PDF de la pièce (JPG, JPEG, PNG, PDF — max 5 Mo)",
                       required = true)
            @RequestParam("fichier") MultipartFile fichier,

            @Parameter(description = "Type de pièce",
                       schema = @Schema(allowableValues = {"CIN", "CIN_RECTO", "CIN_VERSO", "PASSEPORT"}))
            @RequestParam(defaultValue = "CIN") String typePiece,

            @Parameter(description = "Langues Tesseract (ara, fra, ara+fra)",
                       schema = @Schema(defaultValue = "ara+fra"))
            @RequestParam(defaultValue = "ara+fra") String languages) {

        TypePieceJointe type = resoudreType(typePiece);
        CinOcrResponseDTO result = ocrService.extraire(fichier, languages, type);
        return ResponseEntity.ok(result);
    }

    private TypePieceJointe resoudreType(String valeur) {
        if (valeur == null) return TypePieceJointe.AUTRE;
        return switch (valeur.trim().toUpperCase()) {
            case "CIN"        -> TypePieceJointe.CIN_RECTO;
            case "CIN_RECTO"  -> TypePieceJointe.CIN_RECTO;
            case "CIN_VERSO"  -> TypePieceJointe.CIN_VERSO;
            case "PASSEPORT"  -> TypePieceJointe.PASSEPORT;
            default -> {
                try { yield TypePieceJointe.valueOf(valeur.trim().toUpperCase()); }
                catch (IllegalArgumentException e) { yield TypePieceJointe.AUTRE; }
            }
        };
    }
}
