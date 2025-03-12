package br.com.reservei.api.repository;

import br.com.reservei.api.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {

    Optional<Estado> findByNomeOrSigla(String nome, String sigla);
    Optional<Estado> findByNomeOrSiglaAndIdNot(String nome, String sigla, Long id);

}
