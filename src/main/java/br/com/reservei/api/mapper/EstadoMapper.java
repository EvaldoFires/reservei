package br.com.reservei.api.mapper;

import br.com.reservei.api.dto.EstadoDTO;
import br.com.reservei.api.model.Estado;
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
