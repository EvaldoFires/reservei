package br.com.reservei.api.application.usecases.endereco;

import br.com.reservei.api.application.dto.CidadeDTO;
import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoDto;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoSemId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class EnderecoServiceIT {

    @Autowired
    private EstadoServiceImpl estadoService;

    @Autowired
    private CidadeServiceImpl cidadeService;

    @Autowired
    private EnderecoServiceImpl enderecoService;

    private EnderecoDTO enderecoDTO;
    private CidadeDTO cidadeDTO;

    @BeforeEach
    void setUp() {
        var estadoDTO = gerarEstadoDto(gerarEstadoSemId());
        estadoDTO = estadoService.salvar(estadoDTO);
        this.cidadeDTO = gerarCidadeDtoSemId(estadoDTO.id());
        cidadeDTO = cidadeService.salvar(cidadeDTO);
        this.enderecoDTO = gerarEnderecoDtoSemId(cidadeDTO.id());
    }

    @DisplayName("Buscar Endereco")
    @Nested
    class BuscarEndereco {

        @DisplayName("Deve buscar um Endereço pelo ID fornecido")
        @Test
        void deveBuscarEnderecoPorId() {
            enderecoDTO = enderecoService.salvar(enderecoDTO);

            var enderecoRecebido = enderecoService.buscarPorId(enderecoDTO.id());

            assertThat(enderecoRecebido)
                    .usingRecursiveComparison()
                    .isEqualTo(enderecoDTO);
        }

        @DisplayName("Deve lançar exceção ao buscar Endereço com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarEndereco_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> enderecoService.buscarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereço não encontrado com id: " + id);
        }

        @DisplayName("Deve retornar uma lista de endereços salvos")
        @Test
        void deveBuscarTodasAsEndereco() {
            var enderecoDTO1 = enderecoService.salvar(enderecoDTO);
            var enderecoDTO2 = enderecoService.salvar(enderecoDTO);
            var enderecoDTO3 = enderecoService.salvar(enderecoDTO);
            var enderecosSalvas = List.of(enderecoDTO1, enderecoDTO2, enderecoDTO3);

            List<EnderecoDTO> enderecosRecebidos = enderecoService.buscarTodos();

            // Assert
            assertThat(enderecosRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(enderecosSalvas);
        }
    }

    @DisplayName("Cadastrar Endereço")
    @Nested
    class CadastrarEndereco {

        @DisplayName("Deve cadastrar Endereço")
        @Test
        void deveCadastrarEndereco() {
            var enderecoSalva = enderecoService.salvar(enderecoDTO);

            // Assert
            assertThat(enderecoSalva)
                    .isNotNull()
                    .isInstanceOf(EnderecoDTO.class)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(enderecoDTO);

            assertThat(enderecoSalva.id())
                    .isNotNull();
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Endereço com cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoCadastrarEndereco_ComCidadeInexistente() {
            enderecoDTO = new EnderecoDTO(null, 2L, "bairro", "rua", "111");
            assertThatThrownBy(() -> enderecoService.salvar(enderecoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " +
                            enderecoDTO.cidadeId());
        }
    }

    @DisplayName("Alterar Endereço")
    @Nested
    class AlterarEndereco {

        @DisplayName("Deve alterar Endereço cadastrado")
        @Test
        void deveAlterarEnderecoPorId() {
            enderecoDTO = enderecoService.salvar(enderecoDTO);
            var enderecoAtualizada = enderecoService.atualizar(enderecoDTO.id(),
                    new EnderecoDTO(null, enderecoDTO.cidadeId(), "Outro Bairro", "Outra rua", "4"));

            assertThat(enderecoAtualizada)
                    .isNotNull()
                    .isInstanceOf(EnderecoDTO.class)
                    .isNotEqualTo(enderecoDTO);
            assertThat(enderecoAtualizada.id())
                    .isEqualTo(enderecoDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Endereço com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarEndereco_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> enderecoService.atualizar(id, enderecoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereço não encontrado com id: " + id);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Endereço por cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarEndereco_PorCidadeInexistente() {
            Long id = 2L;
            enderecoDTO = enderecoService.salvar(enderecoDTO);
            enderecoDTO = new EnderecoDTO(enderecoDTO.id(), id, "bairro novo", "rua nova", "4");

            assertThatThrownBy(() -> enderecoService.atualizar(enderecoDTO.id(), enderecoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " +
                            id);
        }
    }

    @DisplayName("Deletar Endereço")
    @Nested
    class DeletarEndereco {

        @DisplayName("Deve deletar Endereço")
        @Test
        void deveDeletarEnderecoPorId() {
            enderecoDTO = enderecoService.salvar(enderecoDTO);
            enderecoService.deletarPorId(enderecoDTO.id());

            assertThatThrownBy(() -> enderecoService.buscarPorId(enderecoDTO.id()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereço não encontrado com id: " + enderecoDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Endereço por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarEndereco_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> enderecoService.deletarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereço não encontrado com id: " + id);
        }
    }
}

