package br.com.reservei.api.mapper;

import br.com.reservei.api.dto.RestauranteDTO;
import br.com.reservei.api.model.Restaurante;
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
