package ro.facultate.pos.dto;

import ro.facultate.pos.entity.enums.BonStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BonDetailsResponse {

    private Long id;
    private LocalDateTime data;
    private BonStatus status;

    private Long clientId;
    private Long vanzatorId;

    private List<BonProdusLineResponse> produse;
    private BigDecimal total;

    public BonDetailsResponse() {}

    public Long getId() { return id; }
    public LocalDateTime getData() { return data; }
    public BonStatus getStatus() { return status; }
    public Long getClientId() { return clientId; }
    public Long getVanzatorId() { return vanzatorId; }
    public List<BonProdusLineResponse> getProduse() { return produse; }
    public BigDecimal getTotal() { return total; }

    public void setId(Long id) { this.id = id; }
    public void setData(LocalDateTime data) { this.data = data; }
    public void setStatus(BonStatus status) { this.status = status; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setVanzatorId(Long vanzatorId) { this.vanzatorId = vanzatorId; }
    public void setProduse(List<BonProdusLineResponse> produse) { this.produse = produse; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
