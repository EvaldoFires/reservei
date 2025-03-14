package br.com.reservei.api.mapper;

import br.com.reservei.api.dto.ReservaDTO;
import br.com.reservei.api.model.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "Spring")
public interface ReservaMapper {

    @Mapping(target = "restaurante.id", source = "restauranteId")
    Reserva toEntity (ReservaDTO dto);

    @Mapping(target = "restauranteId", source = "restaurante.id")
    ReservaDTO toDto (Reserva entity);

    @Mapping(target = "restaurante.id", source = "restauranteId")
    @Mapping(target = "id", ignore = true)
    void updateFromDto(ReservaDTO dto, @MappingTarget Reserva entity);
}
