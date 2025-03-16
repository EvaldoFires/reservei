package br.com.reservei.api.usecases;

import br.com.reservei.api.application.dto.AvaliacaoDTO;
import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.restaurante.RestauranteService;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.interfaces.mapper.AvaliacaoMapper;
import br.com.reservei.api.domain.model.Avaliacao;
import br.com.reservei.api.domain.repository.AvaliacaoRepository;
import br.com.reservei.api.application.usecases.avaliacao.AvaliacaoServiceImpl;
import br.com.reservei.api.utils.AvaliacaoHelper;
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

import static br.com.reservei.api.utils.AvaliacaoHelper.gerarAvaliacao;
import static br.com.reservei.api.utils.AvaliacaoHelper.gerarAvaliacaoDto;
import static br.com.reservei.api.utils.RestauranteHelper.gerarRestaurante;
import static br.com.reservei.api.utils.RestauranteHelper.gerarRestauranteDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private AvaliacaoMapper avaliacaoMapper;

    @Mock
    private RestauranteService restauranteService;

    @InjectMocks
    private AvaliacaoServiceImpl avaliacaoService;

    private Avaliacao avaliacao;
    private AvaliacaoDTO avaliacaoDTO;
    private RestauranteDTO restauranteDTO;
    @BeforeEach
    void setUp(){
        this.avaliacao = gerarAvaliacao();
        this.avaliacaoDTO = gerarAvaliacaoDto(avaliacao);
        this.restauranteDTO = gerarRestauranteDto(gerarRestaurante());
    }

    @DisplayName("Buscar Avaliação")
    @Nested
    class BuscarAvaliacao {

        @DisplayName("Deve buscar uma Avaliação pelo ID fornecido")
        @Test
        void deveBuscarAvaliacaoPorId() {
            // Arrange
            when(avaliacaoRepository.findById(avaliacao.getId())).thenReturn(Optional.of(avaliacao));
            when(avaliacaoMapper.toDto(avaliacao)).thenReturn(gerarAvaliacaoDto(avaliacao));

            // Act
            var avaliacaoRecebido = avaliacaoService.buscarPorId(avaliacao.getId());

            // Assert
            assertThat(avaliacaoRecebido)
                    .usingRecursiveComparison()
                    .ignoringFields("restauranteId")
                    .isEqualTo(avaliacao);

            assertThat(avaliacaoRecebido.restauranteId()).isEqualTo(avaliacao.getRestaurante().getId());

            verify(avaliacaoRepository).findById(avaliacao.getId());
        }

        @DisplayName("Deve lançar exceção ao buscar avaliação com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarAvaliacao_PorIdInexistente() {
            // Arrange
            when(avaliacaoRepository.findById(avaliacao.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> avaliacaoService.buscarPorId(avaliacao.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Avaliação não encontrada com id: " + avaliacao.getId());
            verify(avaliacaoRepository).findById(avaliacao.getId());
        }

        @DisplayName("Deve retornar uma lista de avaliações salvas")
        @Test
        void deveBuscarTodasAsAvaliacao() {
            // Arrange
            var avaliacaos = List.of(gerarAvaliacao(), gerarAvaliacao(), gerarAvaliacao());
            var avaliacaosDto = avaliacaos.stream()
                    .map(AvaliacaoHelper::gerarAvaliacaoDto)
                    .toList();

            when(avaliacaoRepository.findAll()).thenReturn(avaliacaos);
            when(avaliacaoMapper.toDto(any(Avaliacao.class)))
                    .thenAnswer(invocation -> {
                        avaliacao = invocation.getArgument(0);
                        return gerarAvaliacaoDto(avaliacao);
                    });

            // Act
            List<AvaliacaoDTO> avaliacaosRecebidos = avaliacaoService.buscarTodos();

            // Assert
            assertThat(avaliacaosRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(avaliacaosDto);
            verify(avaliacaoRepository).findAll();
            verify(avaliacaoMapper, times(3)).toDto(any(Avaliacao.class));
        }
    }

    @DisplayName("Cadastrar Avaliação")
    @Nested
    class CadastrarAvaliacao {

        @DisplayName("Deve cadastrar Avaliação")
        @Test
        void deveCadastrarAvaliacao() {
            // Arrange
            when(avaliacaoMapper.toEntity(avaliacaoDTO)).thenReturn(avaliacao);
            when(avaliacaoMapper.toDto(avaliacao)).thenReturn(avaliacaoDTO);
            when(restauranteService.buscarPorId(avaliacaoDTO.restauranteId())).thenReturn(restauranteDTO);
            when(avaliacaoRepository.save(avaliacao)).thenReturn(avaliacao);

            // Act
            var avaliacaoSalvo = avaliacaoService.salvar(avaliacaoDTO);

            // Assert
            assertThat(avaliacaoSalvo)
                    .isNotNull()
                    .isInstanceOf(AvaliacaoDTO.class)
                    .isEqualTo(avaliacaoDTO);
            verify(restauranteService).buscarPorId(avaliacaoDTO.restauranteId());
            verify(avaliacaoRepository).save(avaliacao);
            verify(avaliacaoMapper).toDto(avaliacao);
            verify(avaliacaoMapper).toEntity(avaliacaoDTO);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Avaliação com restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoCadastrarAvaliacao_ComRestauranteInexistente() {
            // Arrange
            when(restauranteService.buscarPorId(avaliacaoDTO.restauranteId())).thenThrow(new
                    RecursoNaoEncontradoException("Restaurante não encontrado com id: " + avaliacaoDTO.restauranteId()));

            // Act & Assert
            assertThatThrownBy(() -> avaliacaoService.salvar(avaliacaoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + avaliacaoDTO.restauranteId());
            verify(restauranteService).buscarPorId(avaliacaoDTO.restauranteId());
        }
    }

    @DisplayName("Alterar Avaliação")
    @Nested
    class AlterarAvaliacao{

        @DisplayName("Deve alterar Avaliação cadastrada")
        @Test
        void deveAlterarAvaliacaoPorId() {
            // Arrange
            when(avaliacaoMapper.toEntity(avaliacaoDTO)).thenReturn(avaliacao);
            when(avaliacaoMapper.toDto(avaliacao)).thenReturn(avaliacaoDTO);
            doNothing().when(avaliacaoMapper).updateFromDto(avaliacaoDTO, avaliacao);
            when(restauranteService.buscarPorId(avaliacaoDTO.restauranteId())).thenReturn(restauranteDTO);
            when(avaliacaoRepository.save(avaliacao)).thenReturn(avaliacao);
            when(avaliacaoRepository.findById(avaliacao.getId())).thenReturn(Optional.of(avaliacao));

            // Act
            var avaliacaoSalvo = avaliacaoService.atualizar(avaliacaoDTO.id(), avaliacaoDTO);

            // Assert
            assertThat(avaliacaoSalvo)
                    .isNotNull()
                    .isInstanceOf(AvaliacaoDTO.class)
                    .isEqualTo(avaliacaoDTO);
            verify(restauranteService).buscarPorId(avaliacaoDTO.restauranteId());
            verify(avaliacaoRepository).findById(avaliacaoDTO.id());
            verify(avaliacaoRepository).save(avaliacao);
            verify(avaliacaoMapper).updateFromDto(avaliacaoDTO, avaliacao);
            verify(avaliacaoMapper).toEntity(avaliacaoDTO);
            verify(avaliacaoMapper, times(2)).toDto(avaliacao);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Avaliação com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarAvaliacao_PorIdInexistente() {
            // Arrange
            when(avaliacaoRepository.findById(avaliacao.getId())).thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> avaliacaoService.atualizar(avaliacaoDTO.id(), avaliacaoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Avaliação não encontrada com id: " + avaliacao.getId());

            verify(avaliacaoRepository).findById(avaliacaoDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Avaliação por restaurante existente")
        @Test
        void deveGerarExcecao_QuandoAlterarAvaliacao_PorRestauranteInexistente() {
            // Arrange
            when(avaliacaoMapper.toEntity(avaliacaoDTO)).thenReturn(avaliacao);
            when(avaliacaoMapper.toDto(avaliacao)).thenReturn(avaliacaoDTO);
            when(avaliacaoRepository.findById(avaliacao.getId())).thenReturn(Optional.of(avaliacao));
            when(restauranteService.buscarPorId(avaliacaoDTO.restauranteId())).thenThrow(new
                    RecursoNaoEncontradoException("Restaurante não encontrado com id: " + avaliacaoDTO.restauranteId()));

            // Act & Assert
            assertThatThrownBy(() -> avaliacaoService.atualizar(avaliacaoDTO.id(), avaliacaoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + avaliacaoDTO.restauranteId());

            verify(restauranteService).buscarPorId(avaliacaoDTO.restauranteId());
            verify(avaliacaoRepository).findById(avaliacaoDTO.id());
            verifyNoMoreInteractions(avaliacaoRepository);
        }
    }

    @DisplayName("Deletar Avaliação")
    @Nested
    class DeletarAvaliacao{

        @DisplayName("Deve deletar Avaliação")
        @Test
        void deveDeletarAvaliacaoPorId(){
            // Arrange
            when(avaliacaoRepository.findById(avaliacao.getId()))
                    .thenReturn(Optional.of(avaliacao));
            doNothing().when(avaliacaoRepository).deleteById(avaliacao.getId());

            // Act
            avaliacaoService.deletarPorId(avaliacao.getId());

            // Assert
            verify(avaliacaoRepository).findById(avaliacao.getId());
            verify(avaliacaoRepository).deleteById(avaliacao.getId());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Avaliação por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarAvaliacao_PorIdInexistente(){
            // Arrange
            when(avaliacaoRepository.findById(avaliacao.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> avaliacaoService.deletarPorId(avaliacao.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Avaliação não encontrada com id: " + avaliacao.getId());

            verify(avaliacaoRepository).findById(avaliacao.getId());
        }
    }
}
