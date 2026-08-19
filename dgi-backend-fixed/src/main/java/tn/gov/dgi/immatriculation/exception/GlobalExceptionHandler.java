package tn.gov.dgi.immatriculation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import tn.gov.dgi.immatriculation.dto.response.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestionnaire d'erreurs global centralisé. Toute exception non gérée ici
 * remonte en 500 avec un message générique (pour ne jamais exposer de
 * stacktrace ou de détail d'implémentation au client — important pour une
 * application gouvernementale exposée publiquement).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ----------------- Exceptions métier "Not Found" -> 404 -----------------

    @ExceptionHandler({DossierNotFoundException.class, ContribuableNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleNotFound(DgiException ex, HttpServletRequest request) {
        log.info("Ressource non trouvée : {}", ex.getMessage());
        return construireReponse(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request, null);
    }

    // ----------------- Conflit d'état -> 409 -----------------

    @ExceptionHandler(TransitionStatutInvalideException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflit(TransitionStatutInvalideException ex,
                                                          HttpServletRequest request) {
        log.info("Transition de statut refusée : {}", ex.getMessage());
        return construireReponse(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request, null);
    }

    // ----------------- Données/documents invalides -> 400 -----------------

    @ExceptionHandler({DocumentInvalideException.class, OcrExtractionException.class,
            VerificationFacialeEchoueeException.class})
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(DgiException ex, HttpServletRequest request) {
        log.warn("Requête invalide [{}] : {}", ex.getCode(), ex.getMessage());
        return construireReponse(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), request, null);
    }

    // ----------------- Erreurs de validation Bean Validation (@Valid) -> 400 -----------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        List<ErrorResponseDTO.ErreurChamp> erreursChamps = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ErrorResponseDTO.ErreurChamp.builder()
                        .champ(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build())
                .toList();

        log.warn("Erreur de validation sur {} : {} champ(s) invalide(s) → {}",
                request.getRequestURI(), erreursChamps.size(),
                erreursChamps.stream()
                        .map(e -> e.getChamp() + " (" + e.getMessage() + ")")
                        .toList());
        return construireReponse(HttpStatus.BAD_REQUEST, "VALIDATION_ECHOUEE",
                "La requête contient des champs invalides", request, erreursChamps);
    }

    // ----------------- JSON malformé / illisible -> 400 -----------------

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleJsonInvalide(HttpMessageNotReadableException ex,
                                                               HttpServletRequest request) {
        log.warn("Corps de requête illisible sur {} : {}", request.getRequestURI(), ex.getMessage());
        return construireReponse(HttpStatus.BAD_REQUEST, "CORPS_REQUETE_ILLISIBLE",
                "Le corps de la requête est mal formé ou illisible", request, null);
    }

    // ----------------- Upload : fichier manquant ou trop volumineux -----------------

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponseDTO> handleFichierManquant(MissingServletRequestPartException ex,
                                                                  HttpServletRequest request) {
        return construireReponse(HttpStatus.BAD_REQUEST, "FICHIER_MANQUANT",
                "Le fichier attendu (" + ex.getRequestPartName() + ") est manquant dans la requête",
                request, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleTailleDepasse(MaxUploadSizeExceededException ex,
                                                                HttpServletRequest request) {
        return construireReponse(HttpStatus.PAYLOAD_TOO_LARGE, "FICHIER_TROP_VOLUMINEUX",
                "Le fichier envoyé dépasse la taille maximale autorisée par le serveur",
                request, null);
    }

    // ----------------- Filet de sécurité générique -> 500 -----------------

    /**
     * Ne JAMAIS exposer ex.getMessage() ou la stacktrace ici : une
     * exception non prévue peut contenir des détails internes (requête
     * SQL, chemin de fichier serveur...) qu'on ne veut pas exposer à un
     * client externe. Le détail complet part uniquement dans les logs
     * serveur, avec la stacktrace complète pour le diagnostic.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenerique(Exception ex, HttpServletRequest request) {
        log.error("Erreur interne non gérée sur {} :", request.getRequestURI(), ex);
        return construireReponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERREUR_INTERNE",
                "Une erreur interne est survenue. Veuillez réessayer ou contacter le support.",
                request, null);
    }

    // ----------------- Construction de la réponse normalisée -----------------

    private ResponseEntity<ErrorResponseDTO> construireReponse(
            HttpStatus status, String code, String message, HttpServletRequest request,
            List<ErrorResponseDTO.ErreurChamp> erreursChamps) {

        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .erreur(code)
                .message(message)
                .chemin(request.getRequestURI())
                .erreursChamps(erreursChamps)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}