package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request pentru actualizare cantitate pe o linie de bon")
public class UpdateBonProdusRequest {

    @Schema(description = "Noua cantitate", example = "5")
    @NotNull
    @Positive
    private Integer cantitate;

    public UpdateBonProdusRequest() {}

    public Integer getCantitate() { return cantitate; }
    public void setCantitate(Integer cantitate) { this.cantitate = cantitate; }
}
