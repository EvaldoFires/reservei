package br.com.reservei.api.infrastructure.utils;

import br.com.reservei.api.application.dto.ReservaDTO;
import br.com.reservei.api.domain.model.Reserva;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestaurante;

public class ReservaHelper {
    public static Reserva gerarReserva(){
        return Reserva.builder()
                .id(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .restaurante(gerarRestaurante())
                .horaDaReserva(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    public static ReservaDTO gerarReservaDto(Reserva reserva){
        return new ReservaDTO(reserva.getId(),
                reserva.getRestaurante().getId(),
                reserva.getHoraDaReserva());
    }

    public static ReservaDTO gerarReservaDtoSemId(Long restauranteId){
        return new ReservaDTO(null,
                restauranteId,
                LocalDateTime.now()
                        .plusDays(2)
                        .withHour(15)
                        .withMinute(0)
                        .truncatedTo(ChronoUnit.SECONDS));
    }


//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public static Reserva salvarReserva(ReservaRepository reservaRepository,
//                                        RestauranteRepository restauranteRepository){
//        salvarRestaurante(restauranteRepository);
//        var reserva = gerarReserva();
//        return reservaRepository.save(reserva);
//    }
}
