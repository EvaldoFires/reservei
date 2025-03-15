package br.com.reservei.api.utils;

import br.com.reservei.api.dto.ReservaDTO;
import br.com.reservei.api.model.Reserva;
import br.com.reservei.api.repository.ReservaRepository;
import br.com.reservei.api.repository.RestauranteRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static br.com.reservei.api.utils.RestauranteHelper.gerarRestaurante;
import static br.com.reservei.api.utils.RestauranteHelper.salvarRestaurante;

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
