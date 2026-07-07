package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request pentru creare client")
public class CreateClientRequest {

    @Schema(description = "Numele clientului", example = "Maria Ionescu")
    @NotBlank
    @Size(max = 100)
    private String nume;

    @Schema(description = "Email (optional)", example = "maria@example.com")
    @Email
    @Size(max = 120)
    private String email;

    @Schema(description = "Telefon (optional)", example = "0722000000")
    @Size(max = 20)
    private String telefon;

    public CreateClientRequest() {}

    public String getNume() { return nume; }
    public String getEmail() { return email; }
    public String getTelefon() { return telefon; }

    public void setNume(String nume) { this.nume = nume; }
    public void setEmail(String email) { this.email = email; }
    public void setTelefon(String telefon) { this.telefon = telefon; }
}
