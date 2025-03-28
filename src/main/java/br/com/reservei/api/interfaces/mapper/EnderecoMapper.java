package br.com.reservei.api.interfaces.mapper;

import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.domain.model.Endereco;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "Spring")
public interface EnderecoMapper {

    @Mapping(target = "cidade.id", source = "cidadeId")
    Endereco toEntity (EnderecoDTO dto);

    @Mapping(target = "cidadeId", source = "cidade.id")
    EnderecoDTO toDto (Endereco entity);

    @Mapping(target = "cidade.id", source = "cidadeId")
    @Mapping(target = "id", ignore = true)
    void updateFromDto(EnderecoDTO dto, @MappingTarget Endereco entity);
}
