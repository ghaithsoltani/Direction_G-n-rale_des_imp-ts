package tn.gov.dgi.immatriculation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeMinimumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AgeMinimum {

    String message() default "L'âge minimum requis n'est pas atteint";

    int valeur(); // âge minimum en années

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}