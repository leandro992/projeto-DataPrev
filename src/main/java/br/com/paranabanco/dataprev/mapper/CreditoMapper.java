package br.com.paranabanco.dataprev.mapper;

import br.com.paranabanco.dataprev.domain.Beneficio;
import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.domain.OrgaoPagador;
import br.com.paranabanco.dataprev.dto.CreditoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface CreditoMapper {
    
    @Mapping(source = "beneficio.id", target = "beneficioId")
    @Mapping(source = "orgaoPagador.id", target = "orgaoPagadorId")
    CreditoDTO creditoToCreditoDTO(Credito credito);

    @Mapping(target = "beneficio", source = "beneficioId", qualifiedByName = "beneficioIdToBeneficio")
    @Mapping(target = "orgaoPagador", source = "orgaoPagadorId", qualifiedByName = "orgaoPagadorIdToOrgaoPagador")
    @Mapping(target = "unidadeMonetaria", constant = "BRL")
    @Mapping(target = "fimValidade", ignore = true)
    @Mapping(target = "inicioValidade", ignore = true)
    Credito creditoDTOToCredito(CreditoDTO creditoDTO);
    
    @Named("beneficioIdToBeneficio")
    default Beneficio beneficioIdToBeneficio(Long beneficioId) {
        if (beneficioId == null) {
            return null;
        }
        Beneficio beneficio = Beneficio.builder().build();
        beneficio.setId(beneficioId);
        return beneficio;
    }
    
    @Named("orgaoPagadorIdToOrgaoPagador")
    default OrgaoPagador orgaoPagadorIdToOrgaoPagador(Long orgaoPagadorId) {
        if (orgaoPagadorId == null) {
            return null;
        }
        OrgaoPagador orgaoPagador = OrgaoPagador.builder().build();
        orgaoPagador.setId(orgaoPagadorId);
        return orgaoPagador;
    }
}
