package br.com.reservei.api.interfaces.mapper;

import br.com.reservei.api.application.dto.AvaliacaoDTO;
import br.com.reservei.api.domain.model.Avaliacao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "Spring")
public interface AvaliacaoMapper {

    @Mapping(target = "restaurante.id", source = "restauranteId")
    Avaliacao toEntity (AvaliacaoDTO dto);

    @Mapping(target = "restauranteId", source = "restaurante.id")
    AvaliacaoDTO toDto (Avaliacao entity);

    @Mapping(target = "restaurante.id", source = "restauranteId")
    @Mapping(target = "id", ignore = true)
    void updateFromDto(AvaliacaoDTO dto, @MappingTarget Avaliacao entity);
}
