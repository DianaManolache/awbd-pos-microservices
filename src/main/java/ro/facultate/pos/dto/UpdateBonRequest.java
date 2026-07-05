package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request pentru actualizare bon (doar client/vanzator)")
public class UpdateBonRequest {

    @Schema(description = "ID-ul noului client", example = "1")
    @NotNull
    @Positive
    private Long clientId;

    @Schema(description = "ID-ul noului vanzator", example = "1")
    @NotNull
    @Positive
    private Long vanzatorId;

    public UpdateBonRequest() {}

    public Long getClientId() { return clientId; }
    public Long getVanzatorId() { return vanzatorId; }

    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setVanzatorId(Long vanzatorId) { this.vanzatorId = vanzatorId; }
}
