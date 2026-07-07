package ro.facultate.pos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ro.facultate.pos.entity.enums.RolUtilizator;

@Entity
@Table(name = "utilizatori")
@Getter
@Setter
public class Utilizator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @NotBlank
    private String passwordHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RolUtilizator rol;

    @NotNull
    private Boolean activ;

    @NotNull
    @Column(unique = true)
    private Long vanzatorId;

    public Utilizator() {}
}
