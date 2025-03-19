package br.com.reservei.api.application.usecases.endereco;

import br.com.reservei.api.application.dto.CidadeDTO;
import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.domain.exceptions.RecursoJaSalvoException;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.interfaces.mapper.CidadeMapper;
import br.com.reservei.api.domain.model.Cidade;
import br.com.reservei.api.domain.repository.CidadeRepository;
import br.com.reservei.api.infrastructure.utils.CidadeHelper;
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

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidade;
import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDto;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstado;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CidadeServiceTest {

    @Mock
    private CidadeRepository cidadeRepository;

    @Mock
    private CidadeMapper cidadeMapper;

    @Mock
    private EstadoService estadoService;

    @InjectMocks
    private CidadeServiceImpl cidadeService;

    private Cidade cidade;
    private CidadeDTO cidadeDTO;
    private EstadoDTO estadoDTO;

    @BeforeEach
    void setUp() {
        this.cidade = gerarCidade();
        this.cidadeDTO = gerarCidadeDto(cidade);
        this.estadoDTO = gerarEstadoDto(gerarEstado());
    }

    @DisplayName("Buscar Cidade")
    @Nested
    class BuscarCidade {

        @DisplayName("Deve buscar uma Cidade pelo ID fornecido")
        @Test
        void deveBuscarCidadePorId() {
            // Arrange
            when(cidadeRepository.findById(cidade.getId())).thenReturn(Optional.of(cidade));
            when(cidadeMapper.toDto(cidade)).thenReturn(gerarCidadeDto(cidade));

            // Act
            var cidadeRecebido = cidadeService.buscarPorId(cidade.getId());

            // Assert
            assertThat(cidadeRecebido)
                    .usingRecursiveComparison()
                    .ignoringFields("estadoId")
                    .isEqualTo(cidade);

            assertThat(cidadeRecebido.estadoId()).isEqualTo(cidade.getEstado().getId());

            verify(cidadeRepository).findById(cidade.getId());
        }

        @DisplayName("Deve lançar exceção ao buscar cidade com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarCidade_PorIdInexistente() {
            // Arrange
            when(cidadeRepository.findById(cidade.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> cidadeService.buscarPorId(cidade.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " + cidade.getId());

            verify(cidadeRepository).findById(cidade.getId());
        }

        @DisplayName("Deve retornar uma lista de cidades salvos")
        @Test
        void deveBuscarTodosOsCidade() {
            // Arrange
            var cidades = List.of(gerarCidade(), gerarCidade(), gerarCidade());
            var cidadesDto = cidades.stream()
                    .map(CidadeHelper::gerarCidadeDto)
                    .toList();

            when(cidadeRepository.findAll()).thenReturn(cidades);
            when(cidadeMapper.toDto(any(Cidade.class)))
                    .thenAnswer(invocation -> {
                        cidade = invocation.getArgument(0);
                        return gerarCidadeDto(cidade);
                    });

            // Act
            List<CidadeDTO> cidadesRecebidos = cidadeService.buscarTodos();

            // Assert
            assertThat(cidadesRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(cidadesDto);
            verify(cidadeRepository).findAll();
            verify(cidadeMapper, times(3)).toDto(any(Cidade.class));
        }
    }

    @DisplayName("Salvar Cidade")
    @Nested
    class SalvarCidade {

        @DisplayName("Deve salvar Cidade")
        @Test
        void deveSalvarCidade() {
            // Arrange
            when(cidadeMapper.toEntity(cidadeDTO)).thenReturn(cidade);
            when(cidadeMapper.toDto(cidade)).thenReturn(cidadeDTO);
            when(cidadeRepository.save(cidade)).thenReturn(cidade);
            when(estadoService.buscarPorId(cidade.getEstado().getId())).thenReturn(estadoDTO);
            when(cidadeRepository.findByNomeAndEstado_Id(cidade.getNome(), cidade.getEstado().getId()))
                    .thenReturn(Optional.empty());

            // Act
            var cidadeSalvo = cidadeService.salvar(cidadeDTO);

            // Assert
            assertThat(cidadeSalvo)
                    .isNotNull()
                    .isInstanceOf(CidadeDTO.class)
                    .isEqualTo(cidadeDTO);
            verify(cidadeRepository).findByNomeAndEstado_Id(cidade.getNome(), cidade.getEstado().getId());
            verify(cidadeRepository).save(cidade);
            verify(cidadeMapper).toDto(cidade);
            verify(cidadeMapper).toEntity(cidadeDTO);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar cidade com estado e nome já existentes")
        @Test
        void deveGerarExcecao_QuandoSalvarCidade_ComNomeEEstadoExistente() {
            // Arrange
            when(estadoService.buscarPorId(cidadeDTO.estadoId())).thenReturn(estadoDTO);
            when(cidadeRepository.findByNomeAndEstado_Id(cidade.getNome(), cidade.getEstado().getId()))
                    .thenReturn(Optional.of(cidade));

            // Act & Assert
            assertThatThrownBy(() -> cidadeService.salvar(cidadeDTO))
                    .isInstanceOf(RecursoJaSalvoException.class)
                    .hasMessage("Uma cidade com nome '" + cidade.getNome() +
                            "' do estado '" + estadoDTO.nome() + "' já existe no banco de dados.");

            verify(cidadeRepository).findByNomeAndEstado_Id(cidade.getNome(), cidade.getEstado().getId());
        }

        @DisplayName("Deve lançar exceção ao tentar salvar cidade com estado inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarCidade_ComEstadoInexistente() {
            // Arrange
            when(estadoService.buscarPorId(cidadeDTO.estadoId()))
                    .thenThrow(new RecursoNaoEncontradoException("Estado não encontrada com id: " +
                            cidadeDTO.estadoId()));

            // Act & Assert
            assertThatThrownBy(() -> cidadeService.salvar(cidadeDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrada com id: " +
                            cidadeDTO.estadoId());

            verify(estadoService).buscarPorId(cidadeDTO.estadoId());
            verifyNoInteractions(cidadeRepository);
        }
    }

    @DisplayName("Alterar Cidade")
    @Nested
    class AlterarCidade {

        @DisplayName("Deve alterar Cidade cadastrada")
        @Test
        void deveAlterarCidadePorId() {
            // Arrange
            when(cidadeMapper.toEntity(cidadeDTO)).thenReturn(cidade);
            when(cidadeMapper.toDto(cidade)).thenReturn(cidadeDTO);
            doNothing().when(cidadeMapper).updateFromDto(cidadeDTO, cidade);
            when(cidadeRepository.findByNomeAndEstado_IdAndIdNot(
                    cidade.getNome(), cidade.getEstado().getId(), cidade.getId()))
                    .thenReturn(Optional.empty());
            when(cidadeRepository.save(cidade)).thenReturn(cidade);
            when(cidadeRepository.findById(cidade.getId())).thenReturn(Optional.of(cidade));

            // Act
            var cidadeSalvo = cidadeService.atualizar(cidadeDTO.id(), cidadeDTO);

            // Assert
            assertThat(cidadeSalvo)
                    .isNotNull()
                    .isInstanceOf(CidadeDTO.class)
                    .isEqualTo(cidadeDTO);
            verify(cidadeRepository).findByNomeAndEstado_IdAndIdNot(cidadeDTO.nome(),
                    cidadeDTO.estadoId(), cidadeDTO.id());
            verify(cidadeRepository).findById(cidadeDTO.id());
            verify(cidadeRepository).save(cidade);
            verify(cidadeMapper, times(2)).toDto(cidade);
            verify(cidadeMapper).updateFromDto(cidadeDTO, cidade);
            verify(cidadeMapper).toEntity(cidadeDTO);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Cidade com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_PorIdInexistente() {
            // Arrange
            when(cidadeRepository.findById(cidade.getId())).thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> cidadeService.atualizar(cidadeDTO.id(), cidadeDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " + cidade.getId());

            verify(cidadeRepository).findById(cidadeDTO.id());
            verifyNoMoreInteractions(cidadeRepository);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Cidade por cidade existente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_PorCidadeExistente() {
            // Arrange
            when(cidadeMapper.toEntity(cidadeDTO)).thenReturn(cidade);
            when(cidadeMapper.toDto(cidade)).thenReturn(cidadeDTO);
            when(estadoService.buscarPorId(cidadeDTO.estadoId())).thenReturn(estadoDTO);
            when(cidadeRepository.findById(cidade.getId())).thenReturn(Optional.of(cidade));
            when(cidadeRepository.findByNomeAndEstado_IdAndIdNot(cidade.getNome(),
                    cidade.getEstado().getId(), cidade.getId()))
                    .thenReturn(Optional.of(cidade));

            // Act & Assert
            assertThatThrownBy(() -> cidadeService.atualizar(cidadeDTO.id(), cidadeDTO))
                    .isInstanceOf(RecursoJaSalvoException.class)
                    .hasMessage("Uma cidade com nome '" + cidade.getNome() +
                            "' do estado '" + estadoDTO.nome() + "' já existe no banco de dados.");

            verify(cidadeRepository).findByNomeAndEstado_IdAndIdNot(cidade.getNome(),
                    cidade.getEstado().getId(), cidade.getId());
            verify(cidadeRepository).findById(cidadeDTO.id());
            verify(estadoService).buscarPorId(cidadeDTO.estadoId());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Cidade por estado inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_ComEstadoInexistente() {
            // Arrange
            when(cidadeMapper.toEntity(cidadeDTO)).thenReturn(cidade);
            when(cidadeMapper.toDto(cidade)).thenReturn(cidadeDTO);
            when(cidadeRepository.findById(cidade.getId())).thenReturn(Optional.of(cidade));

            when(estadoService.buscarPorId(cidadeDTO.estadoId()))
                    .thenThrow(new RecursoNaoEncontradoException("Estado não encontrada com id: " +
                            cidadeDTO.estadoId()));

            // Act & Assert
            assertThatThrownBy(() -> cidadeService.atualizar(cidadeDTO.id(), cidadeDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrada com id: " +
                            cidadeDTO.estadoId());

            verify(cidadeRepository).findById(cidadeDTO.id());
            verify(estadoService).buscarPorId(cidadeDTO.estadoId());
        }
    }

    @DisplayName("Deletar Cidade")
    @Nested
    class DeletarCidade {

        @DisplayName("Deve deletar Cidade")
        @Test
        void deveDeletarCidadePorId() {
            // Arrange
            when(cidadeRepository.findById(cidade.getId()))
                    .thenReturn(Optional.of(cidade));
            doNothing().when(cidadeRepository).deleteById(cidade.getId());

            // Act
            cidadeService.deletarPorId(cidade.getId());

            // Assert
            verify(cidadeRepository).findById(cidade.getId());
            verify(cidadeRepository).deleteById(cidade.getId());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Cidade por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarCidade_PorIdInexistente() {
            // Arrange
            when(cidadeRepository.findById(cidade.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> cidadeService.deletarPorId(cidade.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " + cidade.getId());

            verify(cidadeRepository).findById(cidade.getId());
        }
    }
}

