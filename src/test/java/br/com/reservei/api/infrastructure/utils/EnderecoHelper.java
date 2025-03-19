package br.com.reservei.api.infrastructure.utils;

import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.domain.model.Endereco;
import br.com.reservei.api.domain.repository.CidadeRepository;
import br.com.reservei.api.domain.repository.EnderecoRepository;
import br.com.reservei.api.domain.repository.EstadoRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.salvarCidade;

public class EnderecoHelper {
    public static Endereco gerarEndereco(){
        return Endereco.builder()
                .id(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .cidade(CidadeHelper.gerarCidade())
                .bairro("Patamares")
                .rua("Ibiassucê")
                .numero("614")
                .build();
    }

    public static EnderecoDTO gerarEnderecoDto(Endereco endereco){
        return new EnderecoDTO(endereco.getId(),
                endereco.getCidade().getId(),
                endereco.getBairro(),
                endereco.getRua(),
                endereco.getNumero());
    }

    public static EnderecoDTO gerarEnderecoDtoSemId(Long cidadeId){
        return new EnderecoDTO(null,
                cidadeId,
                "Centro",
                "Rua Santa Maria",
                "117"
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public static Endereco salvarEndereco(EnderecoRepository enderecoRepository,
                                          CidadeRepository cidadeRepository,
                                          EstadoRepository estadoRepository){
        salvarCidade(cidadeRepository, estadoRepository);
        var endereco = gerarEndereco();
        return enderecoRepository.save(endereco);
    }


}
