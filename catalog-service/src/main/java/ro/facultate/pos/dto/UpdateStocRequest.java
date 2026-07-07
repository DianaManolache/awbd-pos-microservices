package ro.facultate.pos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class UpdateStocRequest {

    @NotNull
    @PositiveOrZero
    private Integer stoc;

    public UpdateStocRequest() {}

    public Integer getStoc() { return stoc; }
    public void setStoc(Integer stoc) { this.stoc = stoc; }
}
