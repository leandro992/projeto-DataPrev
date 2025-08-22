# DataPrev CNAB Writers

O projeto passa a oferecer dois mecanismos para geração de arquivos de remessa:

- **RemessaConcessaoWriter**: implementação original, utilizada diretamente nas etapas do job Spring Batch.
- **CnabWriter**: nova implementação genérica que utiliza `CnabAssembler` e um layout configurável. O layout
  padrão `RemessaConcessaoCnabLayout` reutiliza `RemessaConcessaoLayoutBuilder` sem modificá-lo.

## Selecionando o novo writer

Para utilizar o writer genérico, injete o serviço `RemessaCnabWriterService` e forneça uma implementação de
`CnabLayout`. Exemplo:

```java
RemessaCnabWriterService service = new RemessaCnabWriterService(new RemessaConcessaoCnabLayout());
service.write(remessas, Path.of("/tmp/FSUBCON1n.txt"));
```

Os writers coexistem no projeto; nenhuma alteração foi realizada em `RemessaConcessaoWriter` ou em
`RemessaConcessaoLayoutBuilder`.
