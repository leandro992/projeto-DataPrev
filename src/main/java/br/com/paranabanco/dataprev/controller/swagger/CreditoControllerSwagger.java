package br.com.paranabanco.dataprev.controller.swagger;

import br.com.paranabanco.dataprev.dto.CreditoDTO;
import br.com.paranabanco.dataprev.enumeration.TipoCredito;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;


import java.util.List;

@Tag(name = "Créditos", description = "Operações de consulta e processamento de créditos")
public interface CreditoControllerSwagger {


    ResponseEntity<List<CreditoDTO>> listarTodos();

    ResponseEntity<CreditoDTO> buscarPorId(Long id);

    ResponseEntity<String> processarRemessa();

    ResponseEntity<List<CreditoDTO>> buscarPorTipo( TipoCredito tipoCredito);

    ResponseEntity<CreditoDTO> criar(CreditoDTO creditoDTO);

    ResponseEntity<CreditoDTO> atualizar( Long id, CreditoDTO creditoDTO);

    ResponseEntity<Void> excluir( Long id);




}
