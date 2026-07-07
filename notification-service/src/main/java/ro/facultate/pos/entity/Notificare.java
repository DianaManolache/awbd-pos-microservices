package ro.facultate.pos.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "notificari")
public class Notificare {

    @Id
    private String id;

    private String tip;
    private String mesaj;
    private Map<String, Object> detalii;
    private Instant primitaLa;

    public Notificare() {}

    public Notificare(String tip, String mesaj, Map<String, Object> detalii, Instant primitaLa) {
        this.tip = tip;
        this.mesaj = mesaj;
        this.detalii = detalii;
        this.primitaLa = primitaLa;
    }

    public String getId() { return id; }
    public String getTip() { return tip; }
    public String getMesaj() { return mesaj; }
    public Map<String, Object> getDetalii() { return detalii; }
    public Instant getPrimitaLa() { return primitaLa; }

    public void setId(String id) { this.id = id; }
    public void setTip(String tip) { this.tip = tip; }
    public void setMesaj(String mesaj) { this.mesaj = mesaj; }
    public void setDetalii(Map<String, Object> detalii) { this.detalii = detalii; }
    public void setPrimitaLa(Instant primitaLa) { this.primitaLa = primitaLa; }
}
