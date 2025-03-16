package br.com.reservei.api.interfaces.mapper;

import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.domain.model.Estado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "Spring")
public interface EstadoMapper {

    Estado toEntity (EstadoDTO dto);
    EstadoDTO toDto (Estado entity);
    @Mapping(target = "id", ignore = true)
    void updateFromDto(EstadoDTO dto, @MappingTarget Estado entity);
}
