package tn.gov.dgi.immatriculation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.gov.dgi.immatriculation.model.RoleCibleFaq;

@Data @NoArgsConstructor @AllArgsConstructor
public class FaqEntryCreateDTO {
    @NotBlank private String motsCles;
    @NotBlank private String question;
    @NotBlank private String reponse;
    private String categorie;
    private RoleCibleFaq roleCible;
}