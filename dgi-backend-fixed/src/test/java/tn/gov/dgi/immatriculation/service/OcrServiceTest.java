package tn.gov.dgi.immatriculation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tn.gov.dgi.immatriculation.service.impl.CinArabicParser;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CinArabicParser — no Tesseract dependency, fast.
 * Covers: Arabic labels, Arabic digits, Arabic months, French months,
 * numeric dates, null-safety, confidence scoring.
 */
@DisplayName("CIN Arabic Parser Tests")
class OcrServiceTest {

    // -------------------------------------------------------------------------
    // 1. Arabic digit conversion
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Arabic digit normalisation")
    class ArabicDigits {

        @Test
        @DisplayName("Converts all Arabic digits to Western")
        void convertAll() {
            String input  = "٠١٢٣٤٥٦٧٨٩";
            String result = CinArabicParser.normaliserChiffresArabes(input);
            assertThat(result).isEqualTo("0123456789");
        }

        @Test
        @DisplayName("Leaves Western digits unchanged")
        void leavesWestern() {
            assertThat(CinArabicParser.normaliserChiffresArabes("12345678"))
                    .isEqualTo("12345678");
        }

        @Test
        @DisplayName("Handles mixed Arabic and Western digits")
        void mixedDigits() {
            assertThat(CinArabicParser.normaliserChiffresArabes("١2٣4٥6"))
                    .isEqualTo("123456");
        }
    }

    // -------------------------------------------------------------------------
    // 2. Date parsing — Arabic months
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Arabic month date parsing")
    class ArabicMonths {

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
            "١٢ ماي ٢٠٠٤,       2004-05-12",
            "14 ماي 2004,         2004-05-14",
            "٠١ جانفي ١٩٩٠,      1990-01-01",
            "٢٨ فيفري ٢٠٠٠,      2000-02-28",
            "١٥ أكتوبر ١٩٨٥,     1985-10-15",
            "٣١ ديسمبر ١٩٩٩,     1999-12-31",
        })
        void parsesArabicMonths(String input, LocalDate expected) {
            LocalDate result = CinArabicParser.extraireDate(
                    CinArabicParser.normaliserChiffresArabes(input), input);
            assertThat(result).isEqualTo(expected);
        }
    }

    // -------------------------------------------------------------------------
    // 3. Date parsing — French months
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("French month date parsing")
    class FrenchMonths {

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
            "14 mai 2004,         2004-05-14",
            "01 janvier 1990,     1990-01-01",
            "28 février 2000,     2000-02-28",
            "15 octobre 1985,     1985-10-15",
            "31 décembre 1999,    1999-12-31",
            "3 mars 2010,         2010-03-03",
        })
        void parsesFrenchMonths(String input, LocalDate expected) {
            LocalDate result = CinArabicParser.extraireDate(input, input);
            assertThat(result).isEqualTo(expected);
        }
    }

    // -------------------------------------------------------------------------
    // 4. Date parsing — numeric formats
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Numeric date parsing")
    class NumericDates {

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
            "12/05/1990,  1990-05-12",
            "12-05-1990,  1990-05-12",
            "12.05.1990,  1990-05-12",
        })
        void parsesNumericDates(String input, LocalDate expected) {
            LocalDate result = CinArabicParser.extraireDate(input, input);
            assertThat(result).isEqualTo(expected);
        }
    }

    // -------------------------------------------------------------------------
    // 5. Full CIN card parsing
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Full CIN card OCR text parsing")
    class FullCard {

        @Test
        @DisplayName("Parses realistic CIN Arabic+French OCR output")
        void parsesRealisticCin() {
            // Simulated OCR output from a Tunisian CIN (Arabic side)
            String texte = """
                    الجمهورية التونسية
                    بطاقة التعريف الوطنية
                    اللقب : بن سالم
                    الاسم : محمد أمين
                    تاريخ الولادة : ١٢ ماي ٢٠٠٤
                    مكانها : تونس
                    رقم بطاقة التعريف : ١٢٣٤٥٦٧٨
                    """;

            CinArabicParser.ParseResult r = CinArabicParser.parse(texte);

            assertThat(r.nom).isEqualTo("بن سالم");
            assertThat(r.prenom).isEqualTo("محمد أمين");
            assertThat(r.numeroCin).isEqualTo("12345678");
            assertThat(r.dateNaissance).isEqualTo(LocalDate.of(2004, 5, 12));
            assertThat(r.lieuNaissance).isEqualTo("تونس");
            assertThat(r.confiance).isGreaterThanOrEqualTo(0.9);
        }

        @Test
        @DisplayName("Parses CIN with Western digits and French month")
        void parsesWithFrenchMonth() {
            String texte = """
                    REPUBLIQUE TUNISIENNE
                    CARTE D'IDENTITE NATIONALE
                    اللقب : TRABELSI
                    الاسم : AMINE
                    تاريخ الولادة : 14 mai 2004
                    12345678
                    """;

            CinArabicParser.ParseResult r = CinArabicParser.parse(texte);

            assertThat(r.numeroCin).isEqualTo("12345678");
            assertThat(r.dateNaissance).isEqualTo(LocalDate.of(2004, 5, 14));
            assertThat(r.confiance).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("All fields null on unreadable text — never invents values")
        void returnsNullsForUnreadable() {
            CinArabicParser.ParseResult r = CinArabicParser.parse(
                    "@@## texte illisible sans données ##@@");

            assertThat(r.nom).isNull();
            assertThat(r.prenom).isNull();
            assertThat(r.numeroCin).isNull();
            assertThat(r.dateNaissance).isNull();
            assertThat(r.lieuNaissance).isNull();
            assertThat(r.confiance).isEqualTo(0.0);
        }

        @Test
        @DisplayName("8-digit number extracted from Arabic digits")
        void extractsArabicCinDigits() {
            String texte = "رقم بطاقة التعريف : ٩٨٧٦٥٤٣٢";
            CinArabicParser.ParseResult r = CinArabicParser.parse(texte);
            assertThat(r.numeroCin).isEqualTo("98765432");
        }

        @Test
        @DisplayName("Confidence is proportional to fields found")
        void confidenceScaling() {
            // Only CIN number found
            CinArabicParser.ParseResult partial = CinArabicParser.parse("12345678");
            assertThat(partial.confiance).isEqualTo(0.25); // 1/4 fields

            // All 4 fields found
            String complet = """
                    اللقب : TEST
                    الاسم : TEST
                    تاريخ الولادة : 01/01/2000
                    12345678
                    """;
            CinArabicParser.ParseResult full = CinArabicParser.parse(complet);
            assertThat(full.confiance).isGreaterThanOrEqualTo(0.75);
        }

        @Test
        @DisplayName("Arabic names preserved exactly — not transliterated")
        void arabicNamesPreserved() {
            String texte = """
                    اللقب : القروي
                    الاسم : فاطمة الزهراء
                    """;
            CinArabicParser.ParseResult r = CinArabicParser.parse(texte);
            assertThat(r.nom).isEqualTo("القروي");
            assertThat(r.prenom).isEqualTo("فاطمة الزهراء");
        }

        @Test
        @DisplayName("Handles empty string without throwing")
        void handlesEmptyString() {
            CinArabicParser.ParseResult r = CinArabicParser.parse("");
            assertThat(r).isNotNull();
            assertThat(r.confiance).isEqualTo(0.0);
        }
    }

    // -------------------------------------------------------------------------
    // Legacy test — backward compatibility with existing test name
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Legacy: extracts CIN number and date (backward compat)")
    void parserTexte_devraitExtraireNumeroCinEtDate() {
        String texte = """
                REPUBLIQUE TUNISIENNE
                CARTE D'IDENTITE NATIONALE
                Nom: TRABELSI
                Prenom: AMINE
                Ne le: 12/05/1990
                N° 12345678
                """;
        CinArabicParser.ParseResult r = CinArabicParser.parse(texte);
        assertThat(r.numeroCin).isEqualTo("12345678");
        assertThat(r.dateNaissance).isEqualTo(LocalDate.of(1990, 5, 12));
    }

    @Test
    @DisplayName("Legacy: marks failure if no number detected (backward compat)")
    void parserTexte_devraitMarquerEchecSiAucunNumeroDetecte() {
        CinArabicParser.ParseResult r = CinArabicParser.parse("texte illisible sans numero ####");
        assertThat(r.numeroCin).isNull();
        assertThat(r.confiance).isEqualTo(0.0);
    }
}
