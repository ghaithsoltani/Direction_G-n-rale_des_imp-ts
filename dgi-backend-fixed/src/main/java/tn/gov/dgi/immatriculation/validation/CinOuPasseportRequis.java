package tn.gov.dgi.immatriculation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CinOuPasseportRequisValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CinOuPasseportRequis {

    String message() default "Le CIN ou le numéro de passeport doit être renseigné";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}