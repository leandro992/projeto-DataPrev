package br.com.paranabanco.dataprev.cnab.assembler;

import br.com.paranabanco.dataprev.cnab.layout.CnabLayout;

import java.util.List;

/**
 * Assembles the list of file lines using a provided {@link CnabLayout} implementation.
 */
public class CnabAssembler<T> {

    private final CnabLayout<T> layout;

    public CnabAssembler(CnabLayout<T> layout) {
        this.layout = layout;
    }

    public List<String> assemble(List<T> items) {
        return layout.build(items);
    }
}
