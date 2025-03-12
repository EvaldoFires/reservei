package br.com.reservei.api.mapper;

import br.com.reservei.api.dto.CidadeDTO;
import br.com.reservei.api.model.Cidade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "Spring")
public interface CidadeMapper {

    @Mapping(target = "estado.id", source = "estadoId")
    Cidade toEntity (CidadeDTO dto);

    @Mapping(target = "estadoId", source = "estado.id")
    CidadeDTO toDto (Cidade entity);

    @Mapping(target = "estado.id", source = "estadoId")
    @Mapping(target = "id", ignore = true)
    void updateFromDto(CidadeDTO dto, @MappingTarget Cidade entity);
}
