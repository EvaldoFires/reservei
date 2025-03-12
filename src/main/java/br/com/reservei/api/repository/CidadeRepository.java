package br.com.reservei.api.repository;

import br.com.reservei.api.model.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Long> {
    Optional<Cidade> findByNomeAndEstado_Id(String nome, Long estadoId);
    Optional<Cidade> findByNomeAndEstado_IdAndNotId(String nome, Long estadoId, Long id);
}
