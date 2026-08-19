package tn.gov.dgi.immatriculation.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tn.gov.dgi.immatriculation.model.TypePieceJointe;

/**
 * Converter Spring — permet d'accepter "CIN", "cin", "Cin" etc.
 * dans les @RequestParam et @PathVariable.
 *
 * BUG-ENUM-1 : le frontend envoyait typePiece=CIN mais l'enum
 * ne contenait que CIN_RECTO / CIN_VERSO → 500 ConversionFailedException.
 * Maintenant CIN est une valeur valide ET la conversion est case-insensitive.
 */
@Component
public class TypePieceJointeConverter implements Converter<String, TypePieceJointe> {

    @Override
    public TypePieceJointe convert(String source) {
        return TypePieceJointe.fromString(source);
    }
}