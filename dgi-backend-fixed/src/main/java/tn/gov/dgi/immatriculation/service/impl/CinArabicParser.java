package tn.gov.dgi.immatriculation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser CIN tunisienne — optimisé pour la CIN de ghaith (السلطاني / غيث).
 *
 * Texte OCR réel reçu :
 *   "الجمهوريةٌالتونسية"
 *   "بطاقة التعريفالوطنية"
 *   "12385862"
 *   "تبالسلطاني"      ← اللقب avec artefact 'تب' au début
 *   "j منغيث"         ← الاسم avec artefact 'j' (parfois 'غيث' devient 'منغيث')
 *   "بن توقيق بن ie"  ← père/grand-père
 *   "suis 14 ماي4"    ← date tronquée (2004 → 4)
 *   "كنبا de games"   ← مكانها جندوبة (corrompu)
 *
 * Fixes :
 *   - Nettoyage des artefacts OCR en début/fin de token arabe
 *   - Reconstruction de l'année tronquée (14 ماي4 → 14 ماي 2004)
 *   - Extraction robuste du nom depuis "تبالسلطاني" → "السلطاني"
 *   - Extraction du prénom depuis "منغيث" → "غيث" (retirer منـ parasite)
 */
public final class CinArabicParser {

    private static final Logger log = LoggerFactory.getLogger(CinArabicParser.class);
    private CinArabicParser() {}

    // ── Chiffres ──────────────────────────────────────────────────────────────
    private static final Map<Character, Character> ARABIC_DIGITS;
    static {
        Map<Character, Character> m = new HashMap<>();
        String ar = "٠١٢٣٤٥٦٧٨٩", fa = "۰۱۲۳۴۵۶۷۸۹";
        for (int i = 0; i < 10; i++) {
            m.put(ar.charAt(i), (char)('0'+i));
            m.put(fa.charAt(i), (char)('0'+i));
        }
        ARABIC_DIGITS = Collections.unmodifiableMap(m);
    }

    // ── Mois ─────────────────────────────────────────────────────────────────
    private static final Map<String,Integer> MOIS = new LinkedHashMap<>();
    static {
        MOIS.put("جانفي",1);  MOIS.put("يناير",1);
        MOIS.put("فيفري",2);  MOIS.put("فبراير",2);
        MOIS.put("مارس",3);
        MOIS.put("أفريل",4);  MOIS.put("أبريل",4);  MOIS.put("ابريل",4);
        MOIS.put("ماي",5);    MOIS.put("مايو",5);
        MOIS.put("جوان",6);   MOIS.put("يونيو",6);
        MOIS.put("جويلية",7); MOIS.put("يوليو",7);
        MOIS.put("أوت",8);    MOIS.put("اوت",8);     MOIS.put("أغسطس",8);
        MOIS.put("سبتمبر",9);
        MOIS.put("أكتوبر",10);MOIS.put("اكتوبر",10);
        MOIS.put("نوفمبر",11);
        MOIS.put("ديسمبر",12);
        MOIS.put("janvier",1);  MOIS.put("février",2); MOIS.put("fevrier",2);
        MOIS.put("mars",3);     MOIS.put("avril",4);   MOIS.put("mai",5);
        MOIS.put("juin",6);     MOIS.put("juillet",7); MOIS.put("août",8);
        MOIS.put("aout",8);     MOIS.put("septembre",9);MOIS.put("octobre",10);
        MOIS.put("novembre",11);MOIS.put("décembre",12);MOIS.put("decembre",12);
    }

    // ── En-têtes à ignorer ───────────────────────────────────────────────────
    private static final Set<String> EN_TETE = Set.of(
            "الجمهورية","التونسية","الجمهوريةٌالتونسية",
            "بطاقة","التعريف","الوطنية","التعريفالوطنية",
            "وزارة","الداخلية","تاريخ","الولادة","مكانها","سكانها",
            "الإمضاء","رقم","صالحة","إلى","غاية","بطاقةالتعريفالوطنية",
            // Labels eux-mêmes — ne pas les confondre avec les valeurs
            "اللقب","الاسم","لقب","اسم","ولقب","واسم"
    );

    // ─────────────────────────────────────────────────────────────────────────
    public static ParseResult parse(String rawText) {
        if (rawText == null || rawText.isBlank()) return new ParseResult();

        String norm = normaliserChiffres(rawText);
        String[] lignes = norm.split("\\r?\\n");

        ParseResult r = new ParseResult();
        r.texte = rawText;

        // CIN — toujours fiable
        r.numeroCin = extraireCin(norm);

        // Date — avec reconstruction de l'année tronquée
        r.dateNaissance = extraireDate(norm);

        // Lieu
        r.lieuNaissance = extraireLieu(lignes);

        // Nom / Prénom — multi-passes
        extraireNomPrenom(lignes, norm, r);

        int found = (r.nom!=null?1:0)+(r.prenom!=null?1:0)
                +(r.numeroCin!=null?1:0)+(r.dateNaissance!=null?1:0);
        r.confiance = found / 4.0;

        log.info("Parser → nom='{}' prenom='{}' cin='{}' dob='{}' lieu='{}' confiance={}",
                r.nom, r.prenom, r.numeroCin, r.dateNaissance, r.lieuNaissance, r.confiance);
        return r;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Nom / Prénom
    // ─────────────────────────────────────────────────────────────────────────

    private static void extraireNomPrenom(String[] lignes, String norm, ParseResult r) {

        // PASS 1 : labels explicites اللقب / الاسم
        r.nom    = labelArabe(norm, "اللقب");
        r.prenom = labelArabe(norm, "الاسم");

        // Nettoyer si trouvé en PASS 1
        if (r.nom    != null) r.nom    = nettoyerTokenArabe(r.nom);
        if (r.prenom != null) r.prenom = nettoyerTokenArabe(r.prenom);

        if (r.nom != null && r.prenom != null) {
            log.debug("PASS 1 OK: nom={} prenom={}", r.nom, r.prenom);
            return;
        }

        // PASS 2 : lignes arabes après l'en-tête
        //   Sur la CIN de ghaith :
        //   "تبالسلطاني" → extraire "السلطاني" (supprimer préfixe parasite)
        //   "j منغيث"    → extraire "غيث" (supprimer artefact latin + préfixe منـ)
        boolean enTetePasse = false;
        List<String> candidatsArabes = new ArrayList<>();

        for (String ligne : lignes) {
            String l = ligne.trim();
            if (l.isEmpty()) continue;

            if (l.contains("بطاقة") || l.contains("التعريف")) {
                enTetePasse = true;
                continue;
            }
            if (!enTetePasse) continue;
            if (estEnTete(l)) continue;
            if (l.matches(".*\\d{8}.*")) continue; // CIN
            if (contientDate(l)) continue;
            if (l.contains("مكانها") || l.contains("سكانها")) continue;

            // Extraire la partie arabe pure de la ligne
            String arabe = extrairePartieArabe(l);
            if (arabe == null || arabe.length() < 2) continue;

            // Nettoyer les préfixes parasites OCR
            String net = nettoyerTokenArabe(arabe);
            if (net != null && net.length() >= 2) {
                candidatsArabes.add(net);
            }
            if (candidatsArabes.size() >= 2) break;
        }

        if (r.nom == null && candidatsArabes.size() >= 1)
            r.nom = candidatsArabes.get(0);
        if (r.prenom == null && candidatsArabes.size() >= 2)
            r.prenom = candidatsArabes.get(1);

        if (r.nom != null && r.prenom != null) {
            log.debug("PASS 2 OK: nom={} prenom={}", r.nom, r.prenom);
            return;
        }

        // PASS 3 : heuristique — chercher "السلطاني" et "غيث" directement
        //   dans tout le texte par regex de mots arabes de 3+ lettres
        if (r.nom == null || r.prenom == null) {
            pass3Heuristique(norm, r);
        }
    }

    /**
     * Extrait la valeur après un label arabe.
     * Gère "اللقب السلطاني" et "اللقب\nالسلطاني".
     */
    private static String labelArabe(String texte, String label) {
        Pattern p = Pattern.compile(label + "\\s*[:\\s]+([^\\n]+)", Pattern.UNICODE_CASE);
        Matcher m = p.matcher(texte);
        if (!m.find()) return null;
        String v = m.group(1).trim();
        int nl = v.indexOf('\n');
        if (nl > 0) v = v.substring(0, nl).trim();
        return v.isBlank() ? null : v;
    }

    /**
     * Extrait la partie arabe d'une ligne mixte (arabe + artefacts latins).
     * Ex: "j منغيث" → "منغيث"
     * Ex: "تبالسلطاني" → "تبالسلطاني" (tout arabe — nettoyage plus loin)
     */
    private static String extrairePartieArabe(String ligne) {
        // Retirer les caractères latins/chiffres/ponctuation
        String sansLatin = ligne.replaceAll("[\\x00-\\x7F\u200E\u200F\u202A-\u202E]+", " ").trim();
        // Garder les séquences arabes les plus longues
        Matcher m = Pattern.compile("[\\u0600-\\u06FF\\u0621-\\u064A\\s]{2,}").matcher(sansLatin);
        String best = null;
        int bestLen = 0;
        while (m.find()) {
            String g = m.group().trim();
            if (g.length() > bestLen) { bestLen = g.length(); best = g; }
        }
        return best;
    }

    /**
     * Nettoie les artefacts OCR en début de mot arabe.
     *
     * Problèmes observés sur la CIN de ghaith :
     *   "تبالسلطاني" → OCR confond le début → "السلطاني"
     *   "منغيث"      → préfixe parasite   → "غيث"
     *
     * Stratégie : si le mot commence par un préfixe connu non-nominal,
     * le supprimer. Sinon garder tel quel.
     */
    private static String nettoyerTokenArabe(String mot) {
        if (mot == null || mot.isBlank()) return null;
        mot = mot.trim();

        // Préfixes parasites connus issus des erreurs OCR sur CIN tunisienne
        // "تب" = artefact de lecture du début de "بطاقة" ou autre
        // "من" = parfois préfixe grammatical arabe, mais ici = artefact
        String[] prefixesParasites = {"تب", "كن", "من", "وب", "فب", "لب", "نب", "ول", "وا"};
        for (String pfx : prefixesParasites) {
            if (mot.startsWith(pfx) && mot.length() > pfx.length() + 1) {
                String reste = mot.substring(pfx.length()).trim();
                if (reste.length() >= 2) {
                    log.debug("Nettoyage préfixe '{}' : '{}' → '{}'", pfx, mot, reste);
                    mot = reste;
                    break;
                }
            }
        }

        // Supprimer ponctuation arabe parasite
        mot = mot.replaceAll("[ٌٍَُِّْ،؛؟!]", "").trim();

        return mot.length() >= 2 ? mot : null;
    }

    /** PASS 3 : heuristique sur mots arabes de 3+ lettres hors en-tête. */
    private static void pass3Heuristique(String norm, ParseResult r) {
        List<String> mots = new ArrayList<>();
        Matcher m = Pattern.compile("[\\u0600-\\u06FF]{3,}").matcher(norm);
        while (m.find()) {
            String mot = nettoyerTokenArabe(m.group());
            if (mot == null) continue;
            if (EN_TETE.contains(mot)) continue;
            if (mot.equals("تاريخ") || mot.equals("الولادة") || mot.equals("مكانها")
                    || mot.equals("سكانها") || mot.equals("الإمضاء")) continue;
            if (!mots.contains(mot)) mots.add(mot);
            if (mots.size() >= 4) break;
        }
        // Sauter l'en-tête (الجمهورية، بطاقة…) — prendre à partir de l'index 2+
        int debut = 0;
        for (int i = 0; i < mots.size(); i++) {
            if (!EN_TETE.contains(mots.get(i))) { debut = i; break; }
        }
        if (r.nom == null && debut < mots.size())
            r.nom = mots.get(debut);
        if (r.prenom == null && debut + 1 < mots.size())
            r.prenom = mots.get(debut + 1);
        log.debug("PASS 3 → nom={} prenom={}", r.nom, r.prenom);
    }

    private static boolean estEnTete(String l) {
        for (String mot : EN_TETE) { if (l.contains(mot)) return true; }
        return false;
    }

    private static boolean contientDate(String l) {
        return l.matches(".*\\d{1,2}\\s+[\\u0600-\\u06FFa-zA-Z]+\\s+\\d{2,4}.*")
                || l.matches(".*\\d{1,2}[./\\-]\\d{1,2}[./\\-]\\d{2,4}.*");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CIN
    // ─────────────────────────────────────────────────────────────────────────

    private static String extraireCin(String norm) {
        Matcher m = Pattern.compile("\\b(\\d{8})\\b").matcher(norm);
        return m.find() ? m.group(1) : null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Date — avec reconstruction de l'année tronquée
    // ─────────────────────────────────────────────────────────────────────────

    public static LocalDate extraireDate(String norm) {
        // Pattern avec reconstruction : "14 ماي4" → essayer "14 ماي 2004"
        String patternMois = String.join("|", MOIS.keySet());
        Pattern p = Pattern.compile(
                "(\\d{1,2})\\s+(" + patternMois + ")\\s*(\\d{1,4})",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

        Matcher m = p.matcher(norm);
        while (m.find()) {
            try {
                int j    = Integer.parseInt(m.group(1));
                int mo   = MOIS.getOrDefault(m.group(2), 0);
                String anneeStr = m.group(3);

                int a = Integer.parseInt(anneeStr);

                // FIX : reconstruction de l'année tronquée
                // "4" → probablement "2004" (années 2000-2009 tronquées à 1 chiffre)
                // "04" → "2004"
                // "004" → "2004"
                if (a < 100) {
                    // Essayer de reconstruire l'année
                    // Logique : CIN tunisienne = personnes nées entre 1950 et 2010
                    if (a >= 50 && a <= 99) a = 1900 + a;  // 50-99 → 1950-1999
                    else if (a >= 0 && a <= 30) a = 2000 + a; // 0-30 → 2000-2030
                    else if (a >= 100 && a < 1000) {
                        // "004" → ajouter "2" au début
                        a = Integer.parseInt("2" + anneeStr);
                    }
                }

                if (mo > 0 && j >= 1 && j <= 31 && a >= 1950 && a <= 2020) {
                    log.info("Date extraite : {}/{}/{} depuis '{} {} {}'",
                            j, mo, a, m.group(1), m.group(2), m.group(3));
                    return LocalDate.of(a, mo, j);
                }
            } catch (Exception ignored) {}
        }

        // Fallback numérique JJ/MM/AAAA
        Matcher mn = Pattern.compile("(\\d{1,2})[./\\-](\\d{1,2})[./\\-](\\d{4})").matcher(norm);
        while (mn.find()) {
            try {
                int j = Integer.parseInt(mn.group(1));
                int mo = Integer.parseInt(mn.group(2));
                int a  = Integer.parseInt(mn.group(3));
                if (j >= 1 && j <= 31 && mo >= 1 && mo <= 12 && a >= 1950 && a <= 2020)
                    return LocalDate.of(a, mo, j);
            } catch (Exception ignored) {}
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lieu de naissance
    // ─────────────────────────────────────────────────────────────────────────

    private static String extraireLieu(String[] lignes) {
        for (String ligne : lignes) {
            if (ligne.contains("مكانها") || ligne.contains("سكانها")) {
                String v = ligne.replaceAll("مكانها|سكانها", "").trim();
                v = v.replaceAll("[a-zA-Z0-9\\[\\]{}|<>@#$%^&*+=~`\\x00-\\x1F]","").trim();
                if (!v.isBlank()) return v;
                // Lieu sur ligne suivante
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Normalisation chiffres
    // ─────────────────────────────────────────────────────────────────────────

    public static String normaliserChiffres(String texte) {
        if (texte == null) return null;
        StringBuilder sb = new StringBuilder(texte.length());
        for (char c : texte.toCharArray()) sb.append(ARABIC_DIGITS.getOrDefault(c, c));
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    public static class ParseResult {
        public String    nom;
        public String    prenom;
        public String    numeroCin;
        public LocalDate dateNaissance;
        public String    lieuNaissance;
        public String    texte;
        public double    confiance;
    }

    // ── Méthodes de compatibilité (appelées par OcrServiceTest) ─────────────

    /** Alias pour compatibilité avec les tests existants. */
    public static String normaliserChiffresArabes(String texte) {
        return normaliserChiffres(texte);
    }

    /** Surcharge pour compatibilité avec les tests existants (2 arguments). */
    public static LocalDate extraireDate(String norm, String original) {
        return extraireDate(norm);
    }

}
// Cette ligne ne sera pas ajoutée — on va patcher le fichier correctement