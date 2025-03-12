package br.com.reservei.api.repository;

import br.com.reservei.api.model.Cidade;
import br.com.reservei.api.model.Restaurante;
import br.com.reservei.api.utils.Cozinha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {

    public Optional<Restaurante> findByNome(String nome);
    public List<Restaurante> findByEndereco_Cidade(Cidade cidade);
    public List<Restaurante> findByCozinha(Cozinha cozinha);
}
