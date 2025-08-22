package br.com.paranabanco.dataprev.repository;

import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.enumeration.TipoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditoRepository extends JpaRepository<Credito, Long> {
    /**
     * Encontra uma lista de créditos com base no seu tipo (CONCESSAO, MACICA, etc.).
     * O Spring Data JPA implementa este método automaticamente com base em seu nome.
     *
     * @param tipoCredito O tipo de crédito a ser buscado.
     * @return Uma lista de créditos que correspondem ao tipo fornecido.
     */
    List<Credito> findByTipoCredito(TipoCredito tipoCredito);
}
