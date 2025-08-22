package br.com.paranabanco.dataprev.cnab.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class CnabDomainConversionTest {

    @Test
    void headerArquivoToFromMap() {
        HeaderArquivo original = new HeaderArquivo("001", "20240101");
        Map<String, String> map = original.toMap();
        HeaderArquivo rebuilt = HeaderArquivo.fromMap(map);
        assertEquals(original, rebuilt);
    }

    @Test
    void headerLoteToFromMap() {
        HeaderLote original = new HeaderLote("001", "0001");
        Map<String, String> map = original.toMap();
        HeaderLote rebuilt = HeaderLote.fromMap(map);
        assertEquals(original, rebuilt);
    }

    @Test
    void segmentoAToFromMap() {
        SegmentoA original = new SegmentoA("1234", "56789", "1000");
        Map<String, String> map = original.toMap();
        SegmentoA rebuilt = SegmentoA.fromMap(map);
        assertEquals(original, rebuilt);
    }

    @Test
    void segmentoBToFromMap() {
        SegmentoB original = new SegmentoB("12345678900", "Rua A");
        Map<String, String> map = original.toMap();
        SegmentoB rebuilt = SegmentoB.fromMap(map);
        assertEquals(original, rebuilt);
    }

    @Test
    void trailerLoteToFromMap() {
        TrailerLote original = new TrailerLote("0001", "10");
        Map<String, String> map = original.toMap();
        TrailerLote rebuilt = TrailerLote.fromMap(map);
        assertEquals(original, rebuilt);
    }

    @Test
    void trailerArquivoToFromMap() {
        TrailerArquivo original = new TrailerArquivo("5", "50");
        Map<String, String> map = original.toMap();
        TrailerArquivo rebuilt = TrailerArquivo.fromMap(map);
        assertEquals(original, rebuilt);
    }
}
