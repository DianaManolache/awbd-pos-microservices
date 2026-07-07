package ro.facultate.pos.dto;

import java.math.BigDecimal;

public class BonProdusLineResponse {

    private Long id;
    private Long produsId;
    private String produsNume;
    private Integer cantitate;
    private BigDecimal pretUnitar;
    private BigDecimal totalLinie;

    public BonProdusLineResponse() {}

    public Long getId() { return id; }
    public Long getProdusId() { return produsId; }
    public String getProdusNume() { return produsNume; }
    public Integer getCantitate() { return cantitate; }
    public BigDecimal getPretUnitar() { return pretUnitar; }
    public BigDecimal getTotalLinie() { return totalLinie; }

    public void setId(Long id) { this.id = id; }
    public void setProdusId(Long produsId) { this.produsId = produsId; }
    public void setProdusNume(String produsNume) { this.produsNume = produsNume; }
    public void setCantitate(Integer cantitate) { this.cantitate = cantitate; }
    public void setPretUnitar(BigDecimal pretUnitar) { this.pretUnitar = pretUnitar; }
    public void setTotalLinie(BigDecimal totalLinie) { this.totalLinie = totalLinie; }
}
