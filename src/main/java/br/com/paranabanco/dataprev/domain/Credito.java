package br.com.paranabanco.dataprev.domain;

import br.com.paranabanco.dataprev.enumeration.TipoCredito;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "beneficio_id", nullable = false)
    private Beneficio beneficio;

    @Column(nullable = false)
    private LocalDate fimPeriodo;

    @Column(nullable = false)
    private LocalDate inicioPeriodo;

    @Column(nullable = false)
    private String naturezaCredito;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCredito tipoCredito;

    private LocalDate dataMovimentoCredito;

    @ManyToOne
    @JoinColumn(name = "orgao_pagador_id")
    private OrgaoPagador orgaoPagador;

    private BigDecimal valorLiquidoCredito;

    private BigDecimal valorBrutoCredito;

    private String unidadeMonetaria;

    private LocalDate fimValidade;

    private LocalDate inicioValidade;

    private String indicadorCreditoBloqueado;

    private String origemBloqueio;

    private String numeroContaCorrente;

    private String tipoConta;

    private String origemOrcamento;

    private String indicadorPioneira;

    private String indicadorRepresentanteLegal;

    private String idPregao;

    private String idLotePregao;

    private String idMicrorregiao;

    private String tipoMicroRegiao;

    private LocalDate dataProvaDeVida;

    private String origemProvaVida;

    private String indicadorCreditoCalamidade;

    private String recebedor;
}
