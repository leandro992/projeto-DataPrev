package br.com.paranabanco.dataprev.controller;

import br.com.paranabanco.dataprev.controller.swagger.CreditoControllerSwagger;
import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.dto.CreditoDTO;
import br.com.paranabanco.dataprev.enumeration.TipoCredito;
import br.com.paranabanco.dataprev.mapper.CreditoMapper;
import br.com.paranabanco.dataprev.service.CreditoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/creditos")
public class CreditoController implements CreditoControllerSwagger {

    private final CreditoService creditoService;
    private final CreditoMapper creditoMapper;


    @PostMapping("/processar-remessa")
    @Override
    public ResponseEntity<String> processarRemessa() {
        creditoService.processarRemessaCredito();
        return ResponseEntity.ok("Processamento de remessa iniciado com sucesso");
    }


    @GetMapping
    @Override
    public ResponseEntity<List<CreditoDTO>> listarTodos() {
        return ResponseEntity.ok(
                creditoService.buscarTodos().stream()
                        .map(creditoMapper::creditoToCreditoDTO)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<CreditoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(creditoMapper.creditoToCreditoDTO(creditoService.buscarPorId(id)));
    }

    @GetMapping("/tipo/{tipoCredito}")
    @Override
    public ResponseEntity<List<CreditoDTO>> buscarPorTipo(@PathVariable TipoCredito tipoCredito) {
        return ResponseEntity.ok(creditoService.buscarPorTipoCredito(tipoCredito).stream()
                .map(creditoMapper::creditoToCreditoDTO)
                .toList());
    }

    @PostMapping
    @Override
    public ResponseEntity<CreditoDTO> criar(CreditoDTO creditoDTO) {
        return new ResponseEntity<>(
                creditoService.salvar(creditoMapper.creditoDTOToCredito(creditoDTO)),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<CreditoDTO> atualizar(@PathVariable Long id, @RequestBody CreditoDTO creditoDTO) {
        Credito credito = creditoMapper.creditoDTOToCredito(creditoDTO);
        return ResponseEntity.ok(
                creditoService.salvar(credito)
        );
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        creditoService.excluir(id);
        return ResponseEntity.noContent().build();
    }


}
