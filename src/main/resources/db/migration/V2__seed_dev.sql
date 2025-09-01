/* ============================================================
   Flyway V2__cnab_core.sql (SQL Server)
   Núcleo CNAB: arquivo → lotes → segmentos A/B → trailers
   - Sem "GO"
   - Idempotente
   - Chaves e índices nomeados
   ============================================================ */
SET XACT_ABORT ON;
BEGIN TRAN;

----------------------------------------------------------------
-- 1) Tabela de ARQUIVO (rastreio de processamento)
----------------------------------------------------------------
IF OBJECT_ID('dbo.cnab_arquivo', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cnab_arquivo
    (
        id                   BIGINT IDENTITY(1,1) NOT NULL,
        nome_arquivo         VARCHAR(255)   NOT NULL,  -- ex: HMLCES18.B254...
        rotulo               VARCHAR(30)    NULL,      -- ex: FHMLCES18 / FSUB...
        banco_codigo         CHAR(3)        NULL,      -- "001","237","748"...
        layout_tipo          VARCHAR(10)    NOT NULL,  -- 'CNAB240' | 'CNAB480'
        direcao              VARCHAR(12)    NOT NULL,  -- 'REMESSA' | 'RETORNO'
        data_referencia      DATE           NULL,      -- data competência do arquivo (quando existir)
        hash_conteudo        VARBINARY(32)  NULL,      -- SHA-256 opcional
        tamanho_bytes        BIGINT         NULL,
        status_processamento VARCHAR(20)    NOT NULL CONSTRAINT DF_cnab_arq_status DEFAULT ('RECEBIDO'),
        dt_recebido          DATETIME2(3)   NOT NULL CONSTRAINT DF_cnab_arq_dt_receb DEFAULT (SYSUTCDATETIME()),
        dt_processado        DATETIME2(3)   NULL,

        CONSTRAINT PK_cnab_arquivo PRIMARY KEY CLUSTERED (id)
    );

    CREATE UNIQUE INDEX UX_cnab_arquivo_nome ON dbo.cnab_arquivo (nome_arquivo);
    CREATE INDEX IX_cnab_arquivo_status ON dbo.cnab_arquivo (status_processamento);
END;

----------------------------------------------------------------
-- 2) HEADER DE ARQUIVO (registro tipo 0)
----------------------------------------------------------------
IF OBJECT_ID('dbo.cnab_header_arquivo', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cnab_header_arquivo
    (
        id               BIGINT IDENTITY(1,1) NOT NULL,
        arquivo_id       BIGINT        NOT NULL,
        banco_codigo     CHAR(3)       NOT NULL,
        empresa_nome     VARCHAR(150)  NULL,
        empresa_doc      VARCHAR(14)   NULL,      -- CNPJ/CPF sem máscara
        convenio         VARCHAR(20)   NULL,
        sequencia_arquivo INT          NULL,
        data_geracao     DATE          NULL,
        hora_geracao     CHAR(6)       NULL,      -- HHMMSS

        CONSTRAINT PK_cnab_hdr_arq PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_cnab_hdr_arq_arquivo
            FOREIGN KEY (arquivo_id) REFERENCES dbo.cnab_arquivo(id)
            ON DELETE CASCADE
    );

    CREATE UNIQUE INDEX UX_cnab_hdr_arq_arquivo ON dbo.cnab_header_arquivo (arquivo_id);
END;

----------------------------------------------------------------
-- 3) HEADER DE LOTE (registro tipo 1)
----------------------------------------------------------------
IF OBJECT_ID('dbo.cnab_header_lote', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cnab_header_lote
    (
        id               BIGINT IDENTITY(1,1) NOT NULL,
        arquivo_id       BIGINT       NOT NULL,
        numero_lote      INT          NOT NULL,  -- pos. 4-7 no 240 (ex.)
        operacao         CHAR(1)      NULL,     -- 'C' crédito / 'D' débito
        servico          CHAR(2)      NULL,     -- código serviço
        empresa_agencia  VARCHAR(5)   NULL,
        empresa_conta    VARCHAR(12)  NULL,
        empresa_dac      CHAR(1)      NULL,
        empresa_nome     VARCHAR(150) NULL,
        data_lote        DATE         NULL,

        CONSTRAINT PK_cnab_hdr_lote PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_cnab_hdr_lote UNIQUE (arquivo_id, numero_lote),
        CONSTRAINT FK_cnab_hdr_lote_arquivo
            FOREIGN KEY (arquivo_id) REFERENCES dbo.cnab_arquivo(id)
            ON DELETE CASCADE
    );

    CREATE INDEX IX_cnab_hdr_lote_arquivo ON dbo.cnab_header_lote (arquivo_id);
END;

----------------------------------------------------------------
-- 4) DETALHE - SEGMENTO A (registro tipo 3, segmento 'A')
----------------------------------------------------------------
IF OBJECT_ID('dbo.cnab_detalhe_seg_a', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cnab_detalhe_seg_a
    (
        id                   BIGINT IDENTITY(1,1) NOT NULL,
        arquivo_id           BIGINT      NOT NULL,
        header_lote_id       BIGINT      NOT NULL,
        numero_lote          INT         NOT NULL,
        seq_registro         INT         NOT NULL,  -- sequencial no lote
        banco_favorecido     CHAR(3)     NULL,
        agencia_favorecido   VARCHAR(5)  NULL,
        conta_favorecido     VARCHAR(12) NULL,
        dac_favorecido       CHAR(1)     NULL,
        nome_favorecido      VARCHAR(120) NULL,
        doc_favorecido       VARCHAR(14) NULL,     -- CPF/CNPJ
        nosso_numero         VARCHAR(20) NULL,
        data_pagamento       DATE        NULL,
        moeda                CHAR(3)     NULL,     -- 'BRL'
        valor_pagamento      DECIMAL(18,2) NOT NULL CONSTRAINT DF_segA_valor DEFAULT (0),
        finalidade           VARCHAR(5)  NULL,
        info_complementar    VARCHAR(140) NULL,

        CONSTRAINT PK_cnab_seg_a PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_cnab_seg_a UNIQUE (arquivo_id, numero_lote, seq_registro),
        CONSTRAINT FK_cnab_seg_a_arquivo
            FOREIGN KEY (arquivo_id) REFERENCES dbo.cnab_arquivo(id),
        CONSTRAINT FK_cnab_seg_a_hdr_lote
            FOREIGN KEY (header_lote_id) REFERENCES dbo.cnab_header_lote(id)
            ON DELETE CASCADE
    );

    CREATE INDEX IX_cnab_seg_a_lookup ON dbo.cnab_detalhe_seg_a (arquivo_id, numero_lote);
    CREATE INDEX IX_cnab_seg_a_doc ON dbo.cnab_detalhe_seg_a (doc_favorecido);
END;

----------------------------------------------------------------
-- 5) DETALHE - SEGMENTO B (registro tipo 3, segmento 'B')
--    Dados complementares do favorecido (endereço, doc, etc.)
----------------------------------------------------------------
IF OBJECT_ID('dbo.cnab_detalhe_seg_b', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cnab_detalhe_seg_b
    (
        id                   BIGINT IDENTITY(1,1) NOT NULL,
        arquivo_id           BIGINT      NOT NULL,
        header_lote_id       BIGINT      NOT NULL,
        numero_lote          INT         NOT NULL,
        seq_registro         INT         NOT NULL,  -- mesmo do A correspondente
        endereco             VARCHAR(60) NULL,
        bairro               VARCHAR(30) NULL,
        cidade               VARCHAR(30) NULL,
        uf                   CHAR(2)     NULL,
        cep                  VARCHAR(8)  NULL,
        doc_favorecido       VARCHAR(14) NULL,
        info_complementar    VARCHAR(100) NULL,

        CONSTRAINT PK_cnab_seg_b PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_cnab_seg_b UNIQUE (arquivo_id, numero_lote, seq_registro),
        CONSTRAINT FK_cnab_seg_b_arquivo
            FOREIGN KEY (arquivo_id) REFERENCES dbo.cnab_arquivo(id),
        CONSTRAINT FK_cnab_seg_b_hdr_lote
            FOREIGN KEY (header_lote_id) REFERENCES dbo.cnab_header_lote(id)
            ON DELETE CASCADE
    );

    CREATE INDEX IX_cnab_seg_b_lookup ON dbo.cnab_detalhe_seg_b (arquivo_id, numero_lote);
END;

----------------------------------------------------------------
-- 6) TRAILER DE LOTE (registro tipo 5)
----------------------------------------------------------------
IF OBJECT_ID('dbo.cnab_trailer_lote', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cnab_trailer_lote
    (
        id                    BIGINT IDENTITY(1,1) NOT NULL,
        arquivo_id            BIGINT     NOT NULL,
        numero_lote           INT        NOT NULL,
        qtd_registros         INT        NULL,
        somatorio_valores     DECIMAL(18,2) NULL,

        CONSTRAINT PK_cnab_trl_lote PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_cnab_trl_lote UNIQUE (arquivo_id, numero_lote),
        CONSTRAINT FK_cnab_trl_lote_arquivo
            FOREIGN KEY (arquivo_id) REFERENCES dbo.cnab_arquivo(id)
            ON DELETE CASCADE
    );
END;

----------------------------------------------------------------
-- 7) TRAILER DE ARQUIVO (registro tipo 9)
----------------------------------------------------------------
IF OBJECT_ID('dbo.cnab_trailer_arquivo', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cnab_trailer_arquivo
    (
        id                    BIGINT IDENTITY(1,1) NOT NULL,
        arquivo_id            BIGINT      NOT NULL,
        qtd_lotes             INT         NULL,
        qtd_registros_arquivo INT         NULL,
        somatorio_valores     DECIMAL(18,2) NULL,

        CONSTRAINT PK_cnab_trl_arq PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_cnab_trl_arq UNIQUE (arquivo_id),
        CONSTRAINT FK_cnab_trl_arq_arquivo
            FOREIGN KEY (arquivo_id) REFERENCES dbo.cnab_arquivo(id)
            ON DELETE CASCADE
    );
END;

----------------------------------------------------------------
-- 8) LOG DE ERROS DE PROCESSAMENTO (parser/validações)
----------------------------------------------------------------
IF OBJECT_ID('dbo.cnab_erro', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cnab_erro
    (
        id            BIGINT IDENTITY(1,1) NOT NULL,
        arquivo_id    BIGINT      NOT NULL,
        linha         INT         NULL,        -- linha no arquivo (1-based)
        registro_tipo CHAR(1)     NULL,        -- '0','1','3','5','9'
        segmento      CHAR(1)     NULL,        -- 'A','B'...
        codigo        VARCHAR(50) NOT NULL,    -- ex: 'VAL-CNPJ-INV', 'AG-CONTA-DAC'
        mensagem      VARCHAR(4000) NOT NULL,
        payload       VARCHAR(4000) NULL,      -- trecho da linha ou JSON do campo
        dt_evento     DATETIME2(3) NOT NULL CONSTRAINT DF_cnab_erro_dt DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT PK_cnab_erro PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_cnab_erro_arquivo
            FOREIGN KEY (arquivo_id) REFERENCES dbo.cnab_arquivo(id)
            ON DELETE CASCADE
    );

    CREATE INDEX IX_cnab_erro_arquivo ON dbo.cnab_erro (arquivo_id);
END;

----------------------------------------------------------------
-- 9) REGRAS ÚTEIS / CHECKS
----------------------------------------------------------------
-- Garantir seq_registro > 0
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_segA_seq_pos')
    ALTER TABLE dbo.cnab_detalhe_seg_a ADD CONSTRAINT CK_segA_seq_pos CHECK (seq_registro > 0);

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_segB_seq_pos')
    ALTER TABLE dbo.cnab_detalhe_seg_b ADD CONSTRAINT CK_segB_seq_pos CHECK (seq_registro > 0);

-- Valor de pagamento não negativo
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_segA_valor_nao_neg')
    ALTER TABLE dbo.cnab_detalhe_seg_a ADD CONSTRAINT CK_segA_valor_nao_neg CHECK (valor_pagamento >= 0);

COMMIT TRAN;
