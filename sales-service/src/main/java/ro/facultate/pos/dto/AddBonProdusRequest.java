package ro.facultate.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request pentru adaugare produs pe bon")
public class AddBonProdusRequest {

    @Schema(description = "ID-ul produsului", example = "1")
    @NotNull
    @Positive
    private Long produsId;

    @Schema(description = "Cantitatea dorita", example = "2")
    @NotNull
    @Positive
    private Integer cantitate;

    public AddBonProdusRequest() {}

    public Long getProdusId() { return produsId; }
    public Integer getCantitate() { return cantitate; }

    public void setProdusId(Long produsId) { this.produsId = produsId; }
    public void setCantitate(Integer cantitate) { this.cantitate = cantitate; }
}
