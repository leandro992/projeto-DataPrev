package br.com.paranabanco.dataprev.domain;

import br.com.paranabanco.dataprev.enumeration.EspecieBeneficio;
import br.com.paranabanco.dataprev.enumeration.MotivoCessacaoBeneficio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Beneficio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroBeneficio;

    @Enumerated(EnumType.STRING)
    private EspecieBeneficio especie;

    private LocalDate dataConcessao;

    private LocalDate dataCessacao;

    @Enumerated(EnumType.STRING)
    private MotivoCessacaoBeneficio motivoCessacao;

    @ManyToOne
    @JoinColumn(name = "titular_id", nullable = false)
    private Pessoa titular;

    @ManyToOne
    @JoinColumn(name = "representante_legal_id")
    private Pessoa representanteLegal;

    @ManyToOne
    @JoinColumn(name = "procurador_id")
    private Pessoa procurador;

    private LocalDate validadeProcuracao;

    @ManyToOne
    @JoinColumn(name = "agencia_inss_id")
    private AgenciaINSS agenciaInss;

    @ManyToOne
    @JoinColumn(name = "orgao_pagador_id")
    private OrgaoPagador orgaoPagador;

    private boolean inConcJudSemCpf;
}
