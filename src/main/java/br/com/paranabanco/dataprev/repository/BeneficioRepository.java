package br.com.paranabanco.dataprev.repository;

import br.com.paranabanco.dataprev.domain.Beneficio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {
}
