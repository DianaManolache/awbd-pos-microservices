package ro.facultate.pos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "promotii")
@Getter
@Setter
public class Promotie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nume;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal procentReducere;

    @NotNull
    private LocalDateTime dataStart;

    @NotNull
    private LocalDateTime dataFinal;

    @NotNull
    private Boolean activa;

    @ManyToMany
    @JoinTable(
            name = "promotie_produse",
            joinColumns = @JoinColumn(name = "promotie_id"),
            inverseJoinColumns = @JoinColumn(name = "produs_id")
    )
    private Set<Produs> produse = new HashSet<>();

    public Promotie() {}
}
