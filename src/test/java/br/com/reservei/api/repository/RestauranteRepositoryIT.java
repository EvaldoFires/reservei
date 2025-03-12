package br.com.reservei.api.repository;

import br.com.reservei.api.ReserveiApplication;
import br.com.reservei.api.model.Restaurante;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import static br.com.reservei.api.utils.RestauranteHelper.salvarRestaurante;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(classes = ReserveiApplication.class)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class RestauranteRepositoryIT {

    @Autowired
    private RestauranteRepository restauranteRepository;
    @Autowired
    private EnderecoRepository enderecoRepository;
    @Autowired
    private CidadeRepository cidadeRepository;
    @Autowired
    private EstadoRepository estadoRepository;

    @Test
    void deveCriarTabela(){
        var totalDeRegistros = restauranteRepository.count();
        assertThat(totalDeRegistros).isNotNegative();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveBuscarRestaurantePorNome(){
        // Arrange
        var restaurante = salvarRestaurante(restauranteRepository, enderecoRepository,
                cidadeRepository, estadoRepository);
        var nome = restaurante.getNome();
        // Act
        var restauranteOptional = restauranteRepository.findByNome(nome);
        // Assert
        assertThat(restauranteOptional)
                .isNotNull()
                .isInstanceOf(Restaurante.class)
                .isEqualTo(restaurante);


    }


    @Test
    void deveBuscarRestaurantePorCidade(){
        // Arrange
//        var restaurantes = Arrays.asList(
//                RestauranteHelper.gerarRestaurante(),
//                RestauranteHelper.gerarRestaurante(),
//                RestauranteHelper.gerarRestaurante());
//        var cidade = CidadeHelper.gerarCidade();
//        when(restauranteRepository.findByEndereco_Cidade(cidade)).thenReturn(restaurantes);
//
//        // Act
//        var restaurantesArmazenados = restauranteRepository.findByEndereco_Cidade(cidade);
//
//        // Assert
//        assertThat(restaurantesArmazenados)
//                .isNotNull()
//                .isEqualTo(restaurantes);

        fail("teste não implementado");
    }

    @Test
    void deveBuscarRestaurantePorTipoCozinha(){
//        var restaurantes = Arrays.asList(
//                RestauranteHelper.gerarRestaurante(),
//                RestauranteHelper.gerarRestaurante(),
//                RestauranteHelper.gerarRestaurante());
//        when(restauranteRepository.findByCozinha(Cozinha.ITALIANA)).thenReturn(restaurantes);
//
//        // Act
//        var restaurantesArmazenados = restauranteRepository.findByCozinha(Cozinha.ITALIANA);
//
//        // Assert
//        assertThat(restaurantesArmazenados)
//                .isNotNull()
//                .isEqualTo(restaurantes);
//

        fail("teste não implementado");
    }
}
