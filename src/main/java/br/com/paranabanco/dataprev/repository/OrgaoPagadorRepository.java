package br.com.paranabanco.dataprev.repository;

import br.com.paranabanco.dataprev.domain.OrgaoPagador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgaoPagadorRepository extends JpaRepository<OrgaoPagador, Long> {
}
