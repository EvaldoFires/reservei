package br.com.reservei.api.interfaces.mapper;

import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.domain.model.Restaurante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "Spring")
public interface RestauranteMapper {

    @Mapping(target = "endereco.id", source = "enderecoId")
    Restaurante toEntity (RestauranteDTO dto);

    @Mapping(target = "enderecoId", source = "endereco.id")
    RestauranteDTO toDto (Restaurante entity);

    @Mapping(target = "endereco.id", source = "enderecoId")
    @Mapping(target = "id", ignore = true)
    void updateFromDto(RestauranteDTO dto, @MappingTarget Restaurante entity);
}
