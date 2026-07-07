package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ro.facultate.pos.entity.enums.TipPlata;

@Schema(description = "Request pentru plata bonului")
public class PayBonRequest {

    @Schema(description = "Tipul platii", example = "CASH", allowableValues = {"CASH", "CARD"})
    @NotNull
    private TipPlata tipPlata;

    public PayBonRequest() {}

    public TipPlata getTipPlata() { return tipPlata; }
    public void setTipPlata(TipPlata tipPlata) { this.tipPlata = tipPlata; }
}
