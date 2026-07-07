package ro.facultate.pos.event;

public class StocEpuizatEvent {

    private Long produsId;
    private String nume;
    private Integer stoc;

    public StocEpuizatEvent() {}

    public StocEpuizatEvent(Long produsId, String nume, Integer stoc) {
        this.produsId = produsId;
        this.nume = nume;
        this.stoc = stoc;
    }

    public Long getProdusId() { return produsId; }
    public String getNume() { return nume; }
    public Integer getStoc() { return stoc; }

    public void setProdusId(Long produsId) { this.produsId = produsId; }
    public void setNume(String nume) { this.nume = nume; }
    public void setStoc(Integer stoc) { this.stoc = stoc; }
}
