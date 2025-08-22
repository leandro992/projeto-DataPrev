package br.com.paranabanco.dataprev.service;

import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.dto.CreditoDTO;
import br.com.paranabanco.dataprev.enumeration.TipoCredito;

import java.util.List;

public interface CreditoService {
    List<Credito> buscarTodos();
    Credito buscarPorId(Long id);
    List<Credito> buscarPorTipoCredito(TipoCredito tipoCredito);
    CreditoDTO salvar(Credito credito);
    void excluir(Long id);
    void processarRemessaCredito();
}
