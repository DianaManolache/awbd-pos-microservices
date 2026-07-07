package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Request pentru creare promotie")
public class CreatePromotieRequest {

    @Schema(description = "Numele promotiei", example = "Reduceri de vara")
    @NotBlank
    private String nume;

    @Schema(description = "Procent reducere (0-100)", example = "15")
    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal procentReducere;

    @Schema(description = "Data de start a promotiei")
    @NotNull
    private LocalDateTime dataStart;

    @Schema(description = "Data de final a promotiei")
    @NotNull
    private LocalDateTime dataFinal;

    public CreatePromotieRequest() {}

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public BigDecimal getProcentReducere() { return procentReducere; }
    public void setProcentReducere(BigDecimal procentReducere) { this.procentReducere = procentReducere; }

    public LocalDateTime getDataStart() { return dataStart; }
    public void setDataStart(LocalDateTime dataStart) { this.dataStart = dataStart; }

    public LocalDateTime getDataFinal() { return dataFinal; }
    public void setDataFinal(LocalDateTime dataFinal) { this.dataFinal = dataFinal; }
}
