package br.com.paranabanco.dataprev.cnab;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;

import java.util.LinkedHashMap;
import java.util.Map;

public class CnabLineMapper implements LineMapper<CnabRecord> {
    private final FixedLengthTokenizer tk240;
    private final FixedLengthTokenizer tk480;

    public CnabLineMapper() {
        tk240 = new FixedLengthTokenizer();
        tk240.setStrict(false);
        tk240.setNames("banco","lote","tipoRegistro","resto");
        tk240.setColumns(new Range[]{ new Range(1,3), new Range(4,7), new Range(8,8), new Range(9,240) });

        tk480 = new FixedLengthTokenizer();
        tk480.setStrict(false);
        tk480.setNames("banco","lote","tipoRegistro","resto");
        tk480.setColumns(new Range[]{ new Range(1,3), new Range(4,7), new Range(8,8), new Range(9,480) });
    }

    @Override
    public CnabRecord mapLine(String line, int lineNumber) {
        if (line == null) throw new IllegalArgumentException("Line is null (lineNumber=" + lineNumber + ")");

        final int len = line.length();
        final boolean is480 = len >= 480;
        final int targetSize = is480 ? 480 : 240;

        final FixedLengthTokenizer tk = is480 ? tk480 : tk240;
        FieldSet fs = tk.tokenize(padRight(line, targetSize, ' '));

        String tipoRegistro = fs.readString("tipoRegistro");
        String tipo;
        if ("0".equals(tipoRegistro)) tipo = "HeaderArquivo";
        else if ("1".equals(tipoRegistro)) tipo = "HeaderLote";
        else if ("3".equals(tipoRegistro)) {
            if (len <= 13) throw new IllegalArgumentException("Linha curta p/ ler segmento (pos 14). len=" + len);
            char seg = line.charAt(13);
            tipo = (seg == 'A') ? "SegmentoA" : (seg == 'B') ? "SegmentoB" : "Detalhe";
        } else if ("5".equals(tipoRegistro)) tipo = "TrailerLote";
        else if ("9".equals(tipoRegistro)) tipo = "TrailerArquivo";
        else tipo = "Desconhecido";

        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("banco", fs.readString("banco"));
        campos.put("lote", fs.readString("lote"));
        campos.put("tipoRegistro", tipoRegistro);
        campos.put("resto", fs.readString("resto"));

        return new CnabRecord(tipo, campos, lineNumber);
    }

    /** Gera a linha fixa a partir do record (mínimo viável). */
    public String toLine(CnabRecord rec) {
        int width = inferWidth(rec);
        String banco = fixed(rec.get("banco"), 3);
        String lote  = fixed(rec.get("lote"), 4);
        String tipoR = fixed(rec.get("tipoRegistro"), 1);
        int used = 3 + 4 + 1;
        String resto = fixed(rec.get("resto"), width - used);
        return banco + lote + tipoR + resto; // tamanho garantido
    }

    private int inferWidth(CnabRecord rec) {
        // Heurística simples: se "resto" passar de 232 bytes, assume 480.
        String resto = rec.get("resto");
        return (resto != null && resto.length() > (240 - 8)) ? 480 : 240;
    }

    private static String padRight(String s, int size, char c) {
        if (s == null) s = "";
        if (s.length() >= size) return s.substring(0, size);
        StringBuilder sb = new StringBuilder(size);
        sb.append(s);
        while (sb.length() < size) sb.append(c);
        return sb.toString();
    }

    private static String fixed(String s, int len) {
        if (s == null) s = "";
        return (s.length() >= len) ? s.substring(0, len) : s + " ".repeat(len - s.length());
    }
}
