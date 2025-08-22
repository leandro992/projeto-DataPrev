-- Inserindo Bancos
insert into BANCO (ID, CODIGO_BANCO, NOME_BANCO) values (1, '001', 'Banco do Brasil S.A.');
insert into BANCO (ID, CODIGO_BANCO, NOME_BANCO) values (2, '104', 'Caixa Econômica Federal');

-- Inserindo Órgãos Pagadores (Agências)
insert into ORGAO_PAGADOR (ID, CODIGO_ORGAO_PAGADOR, NOME_AGENCIA, BANCO_ID) values (1, '00101', 'Agência Centro SP', 1);
insert into ORGAO_PAGADOR (ID, CODIGO_ORGAO_PAGADOR, NOME_AGENCIA, BANCO_ID) values (2, '10401', 'Agência Sé', 2);

-- Inserindo Agências INSS
insert into AGENCIA_INSS (ID, CODIGO_AGENCIA, NOME_AGENCIA) values (1, '01001010', 'APS São Paulo - Centro');

-- Inserindo Endereços e Pessoas para os novos benefícios
insert into Endereco (id, logradouro, bairro, municipio, uf, cep) values (10, 'Rua da Concessão, 100', 'Liberdade', 'São Paulo', 'SP', '01501000');
insert into Pessoa (id, nome, cpf, numero_identidade, emissor_identidade, sexo, data_nascimento, nome_mae, email, endereco_id) values (10, 'ANA LIMA', '10101010101', '101010101', 'SECRETARIA_DE_SEGURANCA_PUBLICA', 'FEMININO', '1965-01-01', 'MARIA LIMA', 'ana.lima@example.com', 10);

insert into Endereco (id, logradouro, bairro, municipio, uf, cep) values (11, 'Av. dos Direitos, 200', 'Justiça', 'Brasília', 'DF', '70150900');
insert into Pessoa (id, nome, cpf, numero_identidade, emissor_identidade, sexo, data_nascimento, nome_mae, email, endereco_id) values (11, 'CARLOS MENDES', '20202020202', '202020202', 'POLICIA_FEDERAL', 'MASCULINO', '1970-02-02', 'HELENA MENDES', 'carlos.mendes@example.com', 11);

insert into Endereco (id, logradouro, bairro, municipio, uf, cep) values (12, 'Travessa da Sorte, 300', 'Boa Vista', 'Recife', 'PE', '50010240');
insert into Pessoa (id, nome, cpf, numero_identidade, emissor_identidade, sexo, data_nascimento, nome_mae, email, endereco_id) values (12, 'BRUNO COSTA', '30303030303', '303030303', 'SECRETARIA_DE_SEGURANCA_PUBLICA', 'MASCULINO', '1980-03-03', 'SARA COSTA', 'bruno.costa@example.com', 12);

-- Inserindo Benefícios
-- Benefício Padrão
insert into Beneficio (id, numero_beneficio, especie, data_concessao, titular_id, agencia_inss_id, orgao_pagador_id, in_conc_jud_sem_cpf) values (10, '1000000010', 'APOSENTADORIA_POR_IDADE', '2025-07-10', 10, 1, 1, false);
-- Benefício com marca de CONCESSÃO JUDICIAL
insert into Beneficio (id, numero_beneficio, especie, data_concessao, titular_id, agencia_inss_id, orgao_pagador_id, in_conc_jud_sem_cpf) values (11, '2000000020', 'PENSAO_POR_MORTE', '2025-07-11', 11, 1, 1, true);
-- Benefício Padrão
insert into Beneficio (id, numero_beneficio, especie, data_concessao, titular_id, agencia_inss_id, orgao_pagador_id, in_conc_jud_sem_cpf) values (12, '3000000030', 'APOSENTADORIA_POR_IDADE', '2025-07-12', 12, 1, 2, false);


insert into Credito (beneficio_id, orgao_pagador_id, tipo_credito, valor_liquido_credito, valor_bruto_credito, natureza_credito, data_movimento_credito, inicio_periodo, fim_periodo, inicio_validade, fim_validade, id_pregao) values (10, 1, 'CONCESSAO', 1800.50, 1950.00, '01', '2025-07-20', '2025-07-01', '2025-07-31', '2025-08-01', '2025-09-30', '03');
insert into Credito (beneficio_id, orgao_pagador_id, tipo_credito, valor_liquido_credito, valor_bruto_credito, natureza_credito, data_movimento_credito, inicio_periodo, fim_periodo, inicio_validade, fim_validade, id_pregao) values (11, 1, 'CONCESSAO', 2100.75, 2300.25, '01', '2025-07-20', '2025-07-01', '2025-07-31', '2025-08-01', '2025-09-30', '03');
insert into Credito (beneficio_id, orgao_pagador_id, tipo_credito, valor_liquido_credito, valor_bruto_credito, natureza_credito, data_movimento_credito, inicio_periodo, fim_periodo, inicio_validade, fim_validade, id_pregao) values (12, 2, 'CONCESSAO', 1500.00, 1600.00, '01', '2025-07-20', '2025-07-01', '2025-07-31', '2025-08-01', '2025-09-30', '03');
insert into Credito (beneficio_id, orgao_pagador_id, tipo_credito, valor_liquido_credito, valor_bruto_credito, natureza_credito, data_movimento_credito, inicio_periodo, fim_periodo, inicio_validade, fim_validade, id_pregao) values (12, 2, 'CONCESSAO', 250.00, 250.00, '02', '2025-07-20', '2025-06-01', '2025-06-30', '2025-08-01', '2025-09-30', '03');