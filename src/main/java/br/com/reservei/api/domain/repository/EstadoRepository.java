package br.com.reservei.api.domain.repository;

import br.com.reservei.api.domain.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {

    Optional<Estado> findByNomeOrSigla(String nome, String sigla);

    @Query("SELECT e FROM Estado e WHERE (e.nome = :nome OR e.sigla = :sigla) AND e.id <> :id")
    Optional<Estado> findByNomeOrSiglaAndIdNot(String nome, String sigla, Long id);

}
