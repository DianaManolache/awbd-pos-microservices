package ro.facultate.pos.dto;

import java.math.BigDecimal;

public class ProdusResponse {

    private Long id;
    private String nume;
    private BigDecimal pret;
    private BigDecimal pretEfectiv;
    private Integer stoc;

    public ProdusResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public BigDecimal getPret() { return pret; }
    public void setPret(BigDecimal pret) { this.pret = pret; }

    public BigDecimal getPretEfectiv() { return pretEfectiv; }
    public void setPretEfectiv(BigDecimal pretEfectiv) { this.pretEfectiv = pretEfectiv; }

    public Integer getStoc() { return stoc; }
    public void setStoc(Integer stoc) { this.stoc = stoc; }
}
