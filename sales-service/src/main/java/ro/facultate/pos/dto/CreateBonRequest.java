package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request pentru creare bon (OPEN)")
public class CreateBonRequest {

    @Schema(description = "ID-ul clientului", example = "1")
    @NotNull
    @Positive
    private Long clientId;

    @Schema(description = "ID-ul vanzatorului", example = "1")
    @NotNull
    @Positive
    private Long vanzatorId;

    public CreateBonRequest() {}

    public Long getClientId() { return clientId; }
    public Long getVanzatorId() { return vanzatorId; }

    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setVanzatorId(Long vanzatorId) { this.vanzatorId = vanzatorId; }
}
