package br.com.reservei.api.application.usecases.endereco;

import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.domain.exceptions.RecursoJaSalvoException;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import static br.com.reservei.api.infrastructure.utils.EstadoHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/clean.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Transactional
class EstadoServiceIT {

    @Autowired
    private EstadoService estadoService;

    private EstadoDTO estadoDTO;

    @BeforeEach
    void setUp(){
        this.estadoDTO = gerarEstadoDto(gerarEstadoSemId());
    }

    @DisplayName("Buscar Estado")
    @Nested
    class buscarEstado{

        @DisplayName("Deve buscar um Estado pelo ID fornecido")
        @Test
        void deveBuscarEstadoPorId() {
            var estadoSalvo = estadoService.salvar(estadoDTO);
            var estadoBuscado = estadoService.buscarPorId(estadoSalvo.id());

            assertThat(estadoSalvo)
                    .isNotNull()
                    .isInstanceOf(EstadoDTO.class)
                    .isEqualTo(estadoBuscado);
        }

        @DisplayName("Deve lançar exceção ao buscar Estado com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarEstado_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> estadoService.buscarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " + id);
        }

        @DisplayName("Deve retornar uma lista de estados salvos")
        @Test
        void deveBuscarTodosOsEstado() {
            var estadoDTO1 = estadoService.salvar(new EstadoDTO(null, "Bahia", "BA"));
            var estadoDTO2 = estadoService.salvar(new EstadoDTO(null, "Paraíba", "PB"));
            var estadoDTO3 = estadoService.salvar(new EstadoDTO(null, "Rio de Janeiro", "RJ"));
            var estadosSalvos = List.of(estadoDTO1, estadoDTO2, estadoDTO3);

            // Act
            List<EstadoDTO> estadosRecebidos = estadoService.buscarTodos();

            // Assert
            assertThat(estadosRecebidos)
                    .isNotNull()
                    .hasSize(3)
                    .containsExactlyInAnyOrderElementsOf(estadosSalvos);
        }
    }

    @DisplayName("Salvar Estado")
    @Nested
    class SalvarEstado {

        @DisplayName("Deve salvar Estado")
        @Test
        void deveSalvarEstado() {
            // Arrange

            // Act
            var estadoSalvo = estadoService.salvar(estadoDTO);

            // Assert
            assertThat(estadoSalvo)
                    .isNotNull()
                    .isInstanceOf(EstadoDTO.class)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(estadoDTO);

            assertThat(estadoSalvo.id())
                    .isNotNull();
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Estado com sigla ou nome já existente")
        @Test
        void deveGerarExcecao_QuandoSalvarEstado_ComNomeOuSiglaExistente() {
            estadoService.salvar(estadoDTO);
            assertThatThrownBy(() -> estadoService.salvar(estadoDTO))
                    .isInstanceOf(RecursoJaSalvoException.class)
                    .hasMessage("Um estado com sigla '" + estadoDTO.sigla() +
                            "' ou nome '" + estadoDTO.nome() + "' já existe no banco de dados.");
        }
    }

    @DisplayName("Alterar Estado")
    @Nested
    class AlterarEstado{

        @DisplayName("Deve alterar Estado cadastrada")
        @Test
        void deveAlterarEstadoPorId() {
            var estadoSalvo = estadoService.salvar(estadoDTO);
            var estadoAlterado = estadoService.atualizar(estadoSalvo.id(),
                    new EstadoDTO(null, "Paraíba", "PB"));

            assertThat(estadoAlterado)
                    .isNotNull()
                    .isInstanceOf(EstadoDTO.class)
                    .isNotEqualTo(estadoDTO);

            assertThat(estadoAlterado.id())
                    .isEqualTo(estadoSalvo.id());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Estado com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarEstado_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> estadoService.atualizar(id, estadoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " + id);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Estado por Estado existente")
        @Test
        void deveGerarExcecao_QuandoAlterarEstado_PorEstadoExistente() {
            var estadoSalvo = estadoService.salvar(estadoDTO);
            var estadoSalvo2 = estadoService.salvar(new EstadoDTO(null, "Paraíba", "PB"));

            assertThatThrownBy(() -> estadoService.atualizar(estadoSalvo2.id(), estadoSalvo))
                    .isInstanceOf(RecursoJaSalvoException.class)
                    .hasMessage("Um estado com sigla '" + estadoSalvo.sigla() +
                            "' ou nome '" + estadoSalvo.nome() + "' já existe no banco de dados.");
        }
    }

    @DisplayName("Deletar Estado")
    @Nested
    class DeletarEstado{

        @DisplayName("Deve deletar Estado")
        @Test
        void deveDeletarEstadoPorId(){
            var estadoSalvo = estadoService.salvar(estadoDTO);
            estadoService.deletarPorId(estadoSalvo.id());

            assertThatThrownBy(() -> estadoService.buscarPorId(estadoSalvo.id()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " + estadoSalvo.id());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Estado por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarEstado_PorIdInexistente(){
            Long id = 1L;
            assertThatThrownBy(() -> estadoService.deletarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " + id);
        }
    }
}
