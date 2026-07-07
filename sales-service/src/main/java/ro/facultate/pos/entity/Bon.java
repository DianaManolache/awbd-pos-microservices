package ro.facultate.pos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ro.facultate.pos.entity.enums.BonStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "bonuri")
@Getter
@Setter
public class Bon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDateTime data;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BonStatus status;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "vanzator_id")
    private Vanzator vanzator;

    public Bon() {}
}