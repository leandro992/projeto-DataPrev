# CNAB

Este projeto utiliza layouts JSON para ler e gerar arquivos CNAB.

## Exemplo de leitura
```java
ObjectMapper mapper = new ObjectMapper();
JsonLayout layout = mapper.readValue(
    getClass().getResourceAsStream("/paranabanco-ces.json"),
    JsonLayout.class
);

Files.lines(Paths.get("HMLCES18.B254.D0000001.txt"))
     .forEach(line -> layout.parse(line));
```

## Exemplo de escrita
O batch `RemessaCreditoJobConfig` orquestra a geração do arquivo de remessa:
```java
Job job = context.getBean(RemessaCreditoJobConfig.class).gerarArquivoConcessaoJob();
jobLauncher.run(job, new JobParameters());
```
Internamente o `RemessaConcessaoWriter` utiliza o `RemessaConcessaoLayoutBuilder` para montar cada linha do arquivo.

## Convenções do layout JSON
O arquivo [`paranabanco-ces.json`](src/main/resources/paranabanco-ces.json) define:
- `recordLength`: tamanho fixo de cada linha (240 posições);
- `recordTypeSelector` e `segmentSelector`: posições que identificam tipo e segmento do registro;
- `records`: lista de registros com campos (`name`, `start`, `end`, `type`, etc.).

## Extensão
Para suportar novos bancos ou segmentos:
1. Crie um novo layout JSON ou adicione registros ao existente;
2. Ajuste ou implemente `ItemProcessor`/`ItemWriter` conforme necessário;
3. Configure um novo `Job` em `RemessaCreditoJobConfig` ou outra classe de configuração.

## Pacotes e classes principais
- `br.com.paranabanco.dataprev.job.concessao`
  - `RemessaConcessaoLayoutBuilder`
  - `RemessaConcessaoWriter`
  - `RemessaCreditoProcessor`
- `br.com.paranabanco.dataprev.config`
  - `RemessaCreditoJobConfig`
- `br.com.paranabanco.dataprev.domain` e `br.com.paranabanco.dataprev.service`
  - modelagem de dados e regras de negócio utilizadas durante a leitura e escrita.
