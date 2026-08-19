package tn.gov.dgi.immatriculation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import tn.gov.dgi.immatriculation.dto.request.ContribuableCreateDTO;

public class CinOuPasseportRequisValidator
        implements ConstraintValidator<CinOuPasseportRequis, ContribuableCreateDTO> {

    @Override
    public boolean isValid(ContribuableCreateDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        // Personne morale doesn't need CIN or passport
        if (dto instanceof ContribuableCreateDTO.PersonneMoraleCreateDTO) {
            return true;
        }

        // Personne physique needs at least one
        return StringUtils.isNotBlank(dto.getCin())
                || StringUtils.isNotBlank(dto.getNumeroPasseport());
    }
}