package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request pentru creare categorie")
public class CreateCategorieRequest {

    @Schema(description = "Numele categoriei", example = "Panificatie")
    @NotBlank
    @Size(max = 80)
    private String nume;

    public CreateCategorieRequest() {}

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }
}
