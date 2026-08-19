package tn.gov.dgi.immatriculation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeMinimumValidator implements ConstraintValidator<AgeMinimum, LocalDate> {

    private int ageMinimum;

    @Override
    public void initialize(AgeMinimum annotation) {
        this.ageMinimum = annotation.valeur();
    }

    @Override
    public boolean isValid(LocalDate dateNaissance, ConstraintValidatorContext context) {
        // null est délégué à @NotNull, une contrainte ne doit jamais
        // rejeter une valeur null elle-même (principe Bean Validation)
        if (dateNaissance == null) {
            return true;
        }
        return Period.between(dateNaissance, LocalDate.now()).getYears() >= ageMinimum;
    }
}