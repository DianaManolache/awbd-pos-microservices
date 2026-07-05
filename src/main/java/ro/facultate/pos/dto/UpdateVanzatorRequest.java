package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request pentru actualizare vanzator")
public class UpdateVanzatorRequest {

    @Schema(description = "Numele vanzatorului", example = "Vanzator 1")
    @NotBlank
    @Size(max = 100)
    private String nume;

    public UpdateVanzatorRequest() {}

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }
}
