package br.com.paranabanco.dataprev.job.concessao;


import br.com.paranabanco.dataprev.domain.Beneficio;
import br.com.paranabanco.dataprev.domain.Credito;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class RemessaConcessaoLayoutBuilder {
    private static final DateTimeFormatter DATE_FORMATTER_AAAAMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String buildHeaderLote20e21(String tipoLote, String meioPagto) {
        String sb = StringUtils.rightPad("/", 1) +
                StringUtils.rightPad("", 7) +
                StringUtils.leftPad(tipoLote, 2, '0') +
                StringUtils.leftPad("001", 3, '0') +
                StringUtils.leftPad(meioPagto, 2, '0') +
                StringUtils.rightPad(LocalDate.now().format(DATE_FORMATTER_AAAAMMDD), 8) +
                StringUtils.leftPad("01", 2, '0');
        return StringUtils.rightPad(sb, 480);
    }

    public static String buildDetalheLote20(Credito credito) {
        String sb = StringUtils.rightPad("<", 1) +
                StringUtils.rightPad("", 7) +
                StringUtils.leftPad("20", 2, '0') +
                StringUtils.leftPad(Objects.toString(credito.getBeneficio().getNumeroBeneficio(), ""), 10) +
                StringUtils.rightPad(credito.getFimPeriodo().format(DATE_FORMATTER_AAAAMMDD), 8) +
                StringUtils.rightPad(credito.getInicioPeriodo().format(DATE_FORMATTER_AAAAMMDD), 8) +
                StringUtils.leftPad(credito.getNaturezaCredito(), 2, '0') +
                StringUtils.rightPad(credito.getDataMovimentoCredito().format(DATE_FORMATTER_AAAAMMDD), 8) +
                StringUtils.leftPad(credito.getOrgaoPagador().getCodigoOrgaoPagador(), 6, '0') +
                formatDecimal(credito.getValorLiquidoCredito(), 10, 2);
        return StringUtils.rightPad(sb, 480);
    }

    public static String buildTrailerLote20(int totalRegistros, BigDecimal valorTotal) {
        String sb = StringUtils.rightPad("\\", 1) +
                StringUtils.rightPad("", 7) +
                StringUtils.leftPad("20", 2, '0') +
                StringUtils.leftPad("001", 3, '0') +
                StringUtils.leftPad(String.valueOf(totalRegistros), 8, '0') +
                formatDecimal(valorTotal, 15, 2);
        return StringUtils.rightPad(sb, 480);
    }

    public static String buildDetalheLote21(Beneficio beneficio) {
        String sb = StringUtils.rightPad("<", 1) +
                StringUtils.rightPad("", 7) +
                StringUtils.leftPad("21", 2, '0') +
                StringUtils.leftPad(beneficio.getNumeroBeneficio(), 10) +
                StringUtils.leftPad(beneficio.getOrgaoPagador().getCodigoOrgaoPagador(), 6, '0') +
                StringUtils.leftPad("41", 3, '0') +
                StringUtils.leftPad(beneficio.getAgenciaInss().getCodigoAgencia(), 8, '0') +
                (beneficio.isInConcJudSemCpf() ? "1" : "0");
        return StringUtils.rightPad(sb, 480);
    }

    public static String buildTrailerLote21(int totalRegistros) {
        String sb = StringUtils.rightPad("\\", 1) +
                StringUtils.rightPad("", 7) +
                StringUtils.leftPad("21", 2, '0') +
                StringUtils.leftPad("001", 3, '0') +
                StringUtils.leftPad(String.valueOf(totalRegistros), 8, '0');
        return StringUtils.rightPad(sb, 480);
    }

    private static String formatDecimal(BigDecimal value, int integerPart, int decimalPart) {
        if (value == null) {
            return StringUtils.leftPad("", integerPart + decimalPart, '0');
        }
        String formatted = value.setScale(decimalPart, RoundingMode.HALF_UP).toPlainString().replace(".", "");
        return StringUtils.leftPad(formatted, integerPart + decimalPart, '0');
    }
}