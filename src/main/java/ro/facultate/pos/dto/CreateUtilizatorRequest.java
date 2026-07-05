package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.facultate.pos.entity.enums.RolUtilizator;

@Schema(description = "Request pentru creare utilizator")
public class CreateUtilizatorRequest {

    @Schema(description = "Username unic", example = "ana.vanzatoare")
    @NotBlank
    @Size(max = 50)
    private String username;

    @Schema(description = "Parola in clar (criptarea se aplica la nivel de securitate)", example = "parola123")
    @NotBlank
    @Size(min = 6, max = 255)
    private String password;

    @Schema(description = "Rolul utilizatorului")
    @NotNull
    private RolUtilizator rol;

    @Schema(description = "Id-ul vanzatorului asociat", example = "1")
    @NotNull
    private Long vanzatorId;

    public CreateUtilizatorRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RolUtilizator getRol() { return rol; }
    public void setRol(RolUtilizator rol) { this.rol = rol; }

    public Long getVanzatorId() { return vanzatorId; }
    public void setVanzatorId(Long vanzatorId) { this.vanzatorId = vanzatorId; }
}
