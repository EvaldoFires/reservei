package br.com.reservei.api.application.usecases.restaurante;

import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.endereco.EnderecoService;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.interfaces.mapper.RestauranteMapper;
import br.com.reservei.api.domain.model.Restaurante;
import br.com.reservei.api.domain.repository.RestauranteRepository;
import br.com.reservei.api.infrastructure.utils.RestauranteHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEndereco;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDto;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestaurante;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestauranteServiceTest {

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private RestauranteMapper restauranteMapper;

    @Mock
    private EnderecoService enderecoService;

    @InjectMocks
    private RestauranteServiceImpl restauranteService;

    private Restaurante restaurante;
    private RestauranteDTO restauranteDTO;
    private EnderecoDTO enderecoDTO;
    @BeforeEach
    void setUp(){
        this.restaurante = gerarRestaurante();
        this.restauranteDTO = gerarRestauranteDto(restaurante);
        this.enderecoDTO = gerarEnderecoDto(gerarEndereco());
    }

    @DisplayName("Buscar Restaurante")
    @Nested
    class BuscarRestaurante {

        @DisplayName("Deve buscar um Restaurante pelo ID fornecido")
        @Test
        void deveBuscarRestaurantePorId() {
            // Arrange
            when(restauranteRepository.findById(restaurante.getId())).thenReturn(Optional.of(restaurante));
            when(restauranteMapper.toDto(restaurante)).thenReturn(gerarRestauranteDto(restaurante));

            // Act
            var restauranteRecebido = restauranteService.buscarPorId(restaurante.getId());

            // Assert
            assertThat(restauranteRecebido)
                    .usingRecursiveComparison()
                    .ignoringFields("enderecoId")
                    .isEqualTo(restaurante);
            assertThat(restauranteRecebido.enderecoId()).isEqualTo(restaurante.getEndereco().getId());

            verify(restauranteRepository).findById(restaurante.getId());
        }

        @DisplayName("Deve lançar exceção ao buscar restaurante com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarRestaurante_PorIdInexistente() {
            // Arrange
            when(restauranteRepository.findById(restaurante.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> restauranteService.buscarPorId(restaurante.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + restaurante.getId());
            verify(restauranteRepository).findById(restaurante.getId());
        }

        @DisplayName("Deve retornar uma lista de restaurantes salvos")
        @Test
        void deveBuscarTodosOsRestaurante() {
            // Arrange
            var restaurantes = List.of(gerarRestaurante(), gerarRestaurante(), gerarRestaurante());
            var restaurantesDto = restaurantes.stream()
                    .map(RestauranteHelper::gerarRestauranteDto)
                    .toList();

            when(restauranteRepository.findAll()).thenReturn(restaurantes);
            when(restauranteMapper.toDto(any(Restaurante.class)))
                    .thenAnswer(invocation -> {
                        restaurante = invocation.getArgument(0);
                        return gerarRestauranteDto(restaurante);
                    });

            // Act
            List<RestauranteDTO> restaurantesRecebidos = restauranteService.buscarTodos();

            // Assert
            assertThat(restaurantesRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(restaurantesDto);
            verify(restauranteRepository).findAll();
            verify(restauranteMapper, times(3)).toDto(any(Restaurante.class));
        }
    }

    @DisplayName("Salvar Restaurante")
    @Nested
    class SalvarRestaurante {

        @DisplayName("Deve salvar Restaurante")
        @Test
        void deveSalvarRestaurante() {
            // Arrange
            when(restauranteMapper.toEntity(restauranteDTO)).thenReturn(restaurante);
            when(restauranteMapper.toDto(restaurante)).thenReturn(restauranteDTO);
            when(enderecoService.buscarPorId(restauranteDTO.enderecoId())).thenReturn(enderecoDTO);
            when(restauranteRepository.save(restaurante)).thenReturn(restaurante);

            // Act
            var restauranteSalvo = restauranteService.salvar(restauranteDTO);

            // Assert
            assertThat(restauranteSalvo)
                    .isNotNull()
                    .isInstanceOf(RestauranteDTO.class)
                    .isEqualTo(restauranteDTO);
            verify(enderecoService).buscarPorId(restauranteDTO.enderecoId());
            verify(restauranteRepository).save(restaurante);
            verify(restauranteMapper).toDto(restaurante);
            verify(restauranteMapper).toEntity(restauranteDTO);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Restaurante com endereço inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarRestaurante_ComEnderecoInexistente() {
            // Arrange
            when(enderecoService.buscarPorId(restauranteDTO.enderecoId())).thenThrow(new
                    RecursoNaoEncontradoException("Endereco não encontrado com id: " + restauranteDTO.enderecoId()));

            // Act & Assert
            assertThatThrownBy(() -> restauranteService.salvar(restauranteDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereco não encontrado com id: " + restauranteDTO.enderecoId());
            verify(enderecoService).buscarPorId(restauranteDTO.enderecoId());
        }
    }

    @DisplayName("Alterar Restaurante")
    @Nested
    class AlterarRestaurante{

        @DisplayName("Deve alterar Restaurante cadastrada")
        @Test
        void deveAlterarRestaurantePorId() {
            // Arrange
            when(restauranteMapper.toEntity(restauranteDTO)).thenReturn(restaurante);
            when(restauranteMapper.toDto(restaurante)).thenReturn(restauranteDTO);
            doNothing().when(restauranteMapper).updateFromDto(restauranteDTO, restaurante);
            when(enderecoService.buscarPorId(restauranteDTO.enderecoId())).thenReturn(enderecoDTO);
            when(restauranteRepository.save(restaurante)).thenReturn(restaurante);
            when(restauranteRepository.findById(restaurante.getId())).thenReturn(Optional.of(restaurante));

            // Act
            var restauranteSalvo = restauranteService.atualizar(restauranteDTO.id(), restauranteDTO);

            // Assert
            assertThat(restauranteSalvo)
                    .isNotNull()
                    .isInstanceOf(RestauranteDTO.class)
                    .isEqualTo(restauranteDTO);
            verify(enderecoService).buscarPorId(restauranteDTO.enderecoId());
            verify(restauranteRepository).findById(restauranteDTO.id());
            verify(restauranteRepository).save(restaurante);
            verify(restauranteMapper).updateFromDto(restauranteDTO, restaurante);
            verify(restauranteMapper).toEntity(restauranteDTO);
            verify(restauranteMapper, times(2)).toDto(restaurante);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Restaurante com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarRestaurante_PorIdInexistente() {
            // Arrange
            when(restauranteRepository.findById(restaurante.getId())).thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> restauranteService.atualizar(restauranteDTO.id(), restauranteDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + restaurante.getId());

            verify(restauranteRepository).findById(restauranteDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Restaurante por endereço inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarRestaurante_PorEnderecoInexistente() {
            // Arrange
            when(restauranteMapper.toEntity(restauranteDTO)).thenReturn(restaurante);
            when(restauranteMapper.toDto(restaurante)).thenReturn(restauranteDTO);
            when(restauranteRepository.findById(restaurante.getId())).thenReturn(Optional.of(restaurante));
            when(enderecoService.buscarPorId(restauranteDTO.enderecoId())).thenThrow(new
                    RecursoNaoEncontradoException("Endereco não encontrado com id: " + restauranteDTO.enderecoId()));

            // Act & Assert
            assertThatThrownBy(() -> restauranteService.atualizar(restauranteDTO.id(), restauranteDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereco não encontrado com id: " + restauranteDTO.enderecoId());

            verify(enderecoService).buscarPorId(restauranteDTO.enderecoId());
            verify(restauranteRepository).findById(restauranteDTO.id());
            verifyNoMoreInteractions(restauranteRepository);
        }
    }

    @DisplayName("Deletar Restaurante")
    @Nested
    class DeletarRestaurante{

        @DisplayName("Deve deletar Restaurante")
        @Test
        void deveDeletarRestaurantePorId(){
            // Arrange
            when(restauranteRepository.findById(restaurante.getId()))
                    .thenReturn(Optional.of(restaurante));
            doNothing().when(restauranteRepository).deleteById(restaurante.getId());

            // Act
            restauranteService.deletarPorId(restaurante.getId());

            // Assert
            verify(restauranteRepository).findById(restaurante.getId());
            verify(restauranteRepository).deleteById(restaurante.getId());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Restaurante por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarRestaurante_PorIdInexistente(){
            // Arrange
            when(restauranteRepository.findById(restaurante.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> restauranteService.deletarPorId(restaurante.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + restaurante.getId());

            verify(restauranteRepository).findById(restaurante.getId());
        }
    }
}
