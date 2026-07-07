package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request pentru actualizare produs")
public class UpdateProdusRequest {

    @Schema(description = "Numele produsului", example = "Paine alba")
    @NotBlank
    @Size(max = 120)
    private String nume;

    @Schema(description = "Pretul produsului", example = "3.50")
    @NotNull
    @Positive
    private BigDecimal pret;

    @Schema(description = "Stocul curent", example = "50")
    @NotNull
    @PositiveOrZero
    private Integer stoc;

    @Schema(description = "ID-ul categoriei existente", example = "1")
    @NotNull
    @Positive
    private Long categorieId;

    public UpdateProdusRequest() {}

    public String getNume() { return nume; }
    public BigDecimal getPret() { return pret; }
    public Integer getStoc() { return stoc; }
    public Long getCategorieId() { return categorieId; }

    public void setNume(String nume) { this.nume = nume; }
    public void setPret(BigDecimal pret) { this.pret = pret; }
    public void setStoc(Integer stoc) { this.stoc = stoc; }
    public void setCategorieId(Long categorieId) { this.categorieId = categorieId; }
}
