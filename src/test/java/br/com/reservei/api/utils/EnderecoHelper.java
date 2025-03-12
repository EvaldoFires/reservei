package br.com.reservei.api.utils;

import br.com.reservei.api.model.Endereco;
import br.com.reservei.api.repository.CidadeRepository;
import br.com.reservei.api.repository.EnderecoRepository;
import br.com.reservei.api.repository.EstadoRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static br.com.reservei.api.utils.CidadeHelper.salvarCidade;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public static Endereco salvarEndereco(EnderecoRepository enderecoRepository,
                                          CidadeRepository cidadeRepository,
                                          EstadoRepository estadoRepository){
        salvarCidade(cidadeRepository, estadoRepository);
        var endereco = gerarEndereco();
        return enderecoRepository.save(endereco);
    }


}
