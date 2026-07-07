package ro.facultate.pos.dto;

import jakarta.validation.constraints.NotNull;

public class AjusteazaStocRequest {

    @NotNull
    private Integer delta;

    public AjusteazaStocRequest() {}

    public Integer getDelta() { return delta; }
    public void setDelta(Integer delta) { this.delta = delta; }
}
