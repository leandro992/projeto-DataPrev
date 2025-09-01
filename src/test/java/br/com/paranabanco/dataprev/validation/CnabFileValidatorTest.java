package br.com.paranabanco.dataprev.validation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CnabFileValidatorTest {

    private ValidationResult invokeValidarEstrutura(Path path) throws Exception {
        CnabFileValidator validator = new CnabFileValidator();
        Method method = CnabFileValidator.class.getDeclaredMethod("validarEstrutura", Path.class);
        method.setAccessible(true);
        return (ValidationResult) method.invoke(validator, path);
    }

    @Test
    void aceitaLotes20e21EmPar() throws Exception {
        Path temp = Files.createTempFile("cnab", ".txt");
        List<String> linhas = List.of(
                "00000000", // header arquivo
                "00000201", // header lote 20
                "00000203", // detalhe lote 20
                "00000205", // trailer lote 20
                "00000211", // header lote 21
                "00000213", // detalhe lote 21
                "00000215", // trailer lote 21
                "00000009"  // trailer arquivo
        );
        Files.write(temp, linhas);
        try {
            ValidationResult result = invokeValidarEstrutura(temp);
            assertTrue(result.isValido(), () -> "Esperado arquivo válido, mas foi: " + result.mensagem());
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    void falhaQuandoLote21Ausente() throws Exception {
        Path temp = Files.createTempFile("cnab", ".txt");
        List<String> linhas = List.of(
                "00000000",
                "00000201",
                "00000203",
                "00000205",
                "00000009"
        );
        Files.write(temp, linhas);
        try {
            ValidationResult result = invokeValidarEstrutura(temp);
            assertFalse(result.isValido());
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    void falhaQuandoLote20Ausente() throws Exception {
        Path temp = Files.createTempFile("cnab", ".txt");
        List<String> linhas = List.of(
                "00000000",
                "00000211",
                "00000213",
                "00000215",
                "00000009"
        );
        Files.write(temp, linhas);
        try {
            ValidationResult result = invokeValidarEstrutura(temp);
            assertFalse(result.isValido());
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
