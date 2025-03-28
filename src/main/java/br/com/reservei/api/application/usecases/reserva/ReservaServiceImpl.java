package br.com.reservei.api.application.usecases.reserva;

import br.com.reservei.api.application.dto.ReservaDTO;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.interfaces.mapper.ReservaMapper;
import br.com.reservei.api.domain.model.Reserva;
import br.com.reservei.api.domain.repository.ReservaRepository;
import br.com.reservei.api.application.usecases.restaurante.RestauranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;
    private final RestauranteService restauranteService;

    @Override
    public ReservaDTO buscarPorId(Long id){
        Reserva reserva = reservaRepository.findById(id).orElseThrow(()->
                new RecursoNaoEncontradoException("Reserva não encontrada com id: " + id)
        );
        return reservaMapper.toDto(reserva);
    }

    @Override
    public List<ReservaDTO> buscarTodos() {
        return reservaRepository.findAll()
                .stream()
                .map(reservaMapper::toDto)
                .toList();
    }

    @Override
    public ReservaDTO salvar(ReservaDTO reservaDto) {
        restauranteService.buscarPorId(reservaDto.restauranteId());
        Reserva reserva = reservaMapper.toEntity(reservaDto);
        reserva = reservaRepository.save(reserva);
        return reservaMapper.toDto(reserva);
    }

    @Override
    public ReservaDTO atualizar(Long id, ReservaDTO reservaDto) {
        Reserva reserva = reservaMapper.toEntity(this.buscarPorId(id));
        restauranteService.buscarPorId(reservaDto.restauranteId());
        reservaMapper.updateFromDto(reservaDto, reserva);
        reserva = reservaRepository.save(reserva);
        return reservaMapper.toDto(reserva);
    }

    @Override
    public void deletarPorId(Long id) {
        this.buscarPorId(id);
        reservaRepository.deleteById(id);
    }
}
