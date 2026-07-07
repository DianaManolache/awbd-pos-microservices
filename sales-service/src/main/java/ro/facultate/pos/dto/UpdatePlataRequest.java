package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ro.facultate.pos.entity.enums.TipPlata;

import java.math.BigDecimal;

@Schema(description = "Request pentru actualizare plata")
public class UpdatePlataRequest {

    @Schema(description = "Tipul platii")
    @NotNull
    private TipPlata tip;

    @Schema(description = "Suma platii", example = "20.00")
    @NotNull
    @Positive
    private BigDecimal suma;

    public UpdatePlataRequest() {}

    public TipPlata getTip() { return tip; }
    public void setTip(TipPlata tip) { this.tip = tip; }

    public BigDecimal getSuma() { return suma; }
    public void setSuma(BigDecimal suma) { this.suma = suma; }
}
