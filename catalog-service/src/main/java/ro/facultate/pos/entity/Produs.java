package ro.facultate.pos.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "produse")
@Getter
@Setter
public class Produs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nume;

    @NotNull
    @Positive
    private BigDecimal pret;

    @NotNull
    @PositiveOrZero
    private Integer stoc;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToMany(mappedBy = "produse")
    @JsonIgnore
    private Set<Promotie> promotii = new HashSet<>();

    /**
     * Pretul dupa aplicarea celei mai mari reduceri active in acest moment
     * (sau egal cu pret, daca nu exista nicio promotie activa). Calculat
     * la fiecare request de ProdusService, nu persistat si nu cache-uit -
     * "activ acum" se schimba in timp, deci ar deveni stale daca ar fi
     * inghetat in valoarea pusa in cache-ul Redis.
     */
    @Transient
    private BigDecimal pretEfectiv;

    public Produs() {}
}
