package br.com.reservei.api.repository;

import br.com.reservei.api.utils.CidadeHelper;
import br.com.reservei.api.utils.Cozinha;
import br.com.reservei.api.utils.RestauranteHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestauranteRepositoryTest {

    @Mock
    private RestauranteRepository restauranteRepository;

    @Test
    void deveBuscarRestaurantePorNome(){
        // Arrange
        var restaurante = RestauranteHelper.gerarRestaurante();

        when(restauranteRepository.findByNome(restaurante.getNome()))
                .thenReturn(Optional.of(restaurante));
        // Act
        var restauranteArmazenado = restauranteRepository.findByNome(restaurante.getNome());
        // Assert
        assertThat(restauranteArmazenado)
                .isPresent()
                .isNotNull()
                .isEqualTo(Optional.of(restaurante));

        verify(restauranteRepository, times(1)).findByNome(restaurante.getNome());

    }

    @Test
    void deveBuscarRestaurantePorCidade(){
        // Arrange
        var restaurantes = Arrays.asList(
                RestauranteHelper.gerarRestaurante(),
                RestauranteHelper.gerarRestaurante(),
                RestauranteHelper.gerarRestaurante());
        var cidade = CidadeHelper.gerarCidade();
        when(restauranteRepository.findByEndereco_Cidade(cidade)).thenReturn(restaurantes);

        // Act
        var restaurantesArmazenados = restauranteRepository.findByEndereco_Cidade(cidade);

        // Assert
        assertThat(restaurantesArmazenados)
                .isNotNull()
                .isEqualTo(restaurantes);

        verify(restauranteRepository, times(1)).findByEndereco_Cidade(cidade);
    }

    @Test
    void deveBuscarRestaurantePorTipoCozinha(){
        var restaurantes = Arrays.asList(
                RestauranteHelper.gerarRestaurante(),
                RestauranteHelper.gerarRestaurante(),
                RestauranteHelper.gerarRestaurante());
        when(restauranteRepository.findByCozinha(Cozinha.ITALIANA)).thenReturn(restaurantes);

        // Act
        var restaurantesArmazenados = restauranteRepository.findByCozinha(Cozinha.ITALIANA);

        // Assert
        assertThat(restaurantesArmazenados)
                .isNotNull()
                .isEqualTo(restaurantes);

        verify(restauranteRepository, times(1)).findByCozinha(Cozinha.ITALIANA);

    }


}
