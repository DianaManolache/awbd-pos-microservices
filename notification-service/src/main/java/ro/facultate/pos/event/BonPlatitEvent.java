package ro.facultate.pos.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BonPlatitEvent {

    private Long bonId;
    private Long clientId;
    private Long vanzatorId;
    private BigDecimal total;
    private String tipPlata;
    private LocalDateTime data;

    public BonPlatitEvent() {}

    public Long getBonId() { return bonId; }
    public Long getClientId() { return clientId; }
    public Long getVanzatorId() { return vanzatorId; }
    public BigDecimal getTotal() { return total; }
    public String getTipPlata() { return tipPlata; }
    public LocalDateTime getData() { return data; }

    public void setBonId(Long bonId) { this.bonId = bonId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setVanzatorId(Long vanzatorId) { this.vanzatorId = vanzatorId; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setTipPlata(String tipPlata) { this.tipPlata = tipPlata; }
    public void setData(LocalDateTime data) { this.data = data; }
}
