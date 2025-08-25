package br.com.paranabanco.dataprev.utils;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public final class PositionalFormatter {

    private final CnabLineMapper cnabLineMapper;
    private final Charset charset;

    public PositionalFormatter(CnabLineMapper cnabLineMapper) {
        this(cnabLineMapper, StandardCharsets.ISO_8859_1);
    }

    public PositionalFormatter(CnabLineMapper cnabLineMapper, Charset charset) {
        this.cnabLineMapper = cnabLineMapper;
        this.charset = charset;
    }

    /** Converte o record para linha posicional (240/480). */
    public String format(CnabRecord rec) {
        return cnabLineMapper.toLine(rec);
    }

    public Charset getCharset() {
        return charset;
    }
}
