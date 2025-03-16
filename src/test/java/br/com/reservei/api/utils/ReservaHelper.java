package br.com.reservei.api.utils;

import br.com.reservei.api.application.dto.ReservaDTO;
import br.com.reservei.api.domain.model.Reserva;

import java.time.LocalDateTime;
import java.util.UUID;

import static br.com.reservei.api.utils.RestauranteHelper.gerarRestaurante;

public class ReservaHelper {
    public static Reserva gerarReserva(){
        return Reserva.builder()
                .id(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .restaurante(gerarRestaurante())
                .horaDaReserva(LocalDateTime.now())
                .build();
    }

    public static ReservaDTO gerarReservaDto(Reserva reserva){
        return new ReservaDTO(reserva.getId(),
                reserva.getRestaurante().getId(),
                reserva.getHoraDaReserva());
    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public static Reserva salvarReserva(ReservaRepository reservaRepository,
//                                        RestauranteRepository restauranteRepository){
//        salvarRestaurante(restauranteRepository);
//        var reserva = gerarReserva();
//        return reservaRepository.save(reserva);
//    }
}
