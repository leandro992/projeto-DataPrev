package br.com.paranabanco.dataprev.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrgaoPagador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigoOrgaoPagador;

    private String nomeAgencia;

    @ManyToOne
    @JoinColumn(name = "banco_id", nullable = false)
    private Banco banco;
}
