package ro.facultate.pos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import ro.facultate.pos.entity.enums.StatusPlata;
import ro.facultate.pos.entity.enums.TipPlata;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "plati")
@Getter
@Setter
public class Plata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "bon_id")
    private Bon bon;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipPlata tip;

    @NotNull
    @Positive
    private BigDecimal suma;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusPlata status;

    @NotNull
    private LocalDateTime data;

    public Plata() {}
}
