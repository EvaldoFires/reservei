package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.application.usecases.endereco.EnderecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/endereco")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping("/{idEndereco}")
    @Operation(summary = "Buscar Endereco por ID", description = "Busca um Endereco pelo seu ID")
            @ApiResponse(responseCode = "200", description = "Endereco encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EnderecoDTO.class)))
            @ApiResponse(responseCode = "404", description = "Endereco nao encontrado")
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<EnderecoDTO> buscarPorId(@PathVariable Long idEndereco){
        return ResponseEntity.ok(enderecoService.buscarPorId(idEndereco));
    }

    @GetMapping
    @Operation(summary = "Buscar todos os Enderecos", description = "Busca uma lista com todos os Enderecos")
    @ApiResponse(responseCode = "200", description = "Enderecos encontrados com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EnderecoDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<List<EnderecoDTO>> buscarTodos(){
        return ResponseEntity.ok(enderecoService.buscarTodos());
    }

    @PostMapping
    @Operation(summary = "Salva um Endereco", description = "Salva um Endereco")
    @ApiResponse(responseCode = "201", description = "Endereco salvo com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EnderecoDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<EnderecoDTO> salvar(@RequestBody EnderecoDTO enderecoDTO){
        enderecoDTO = enderecoService.salvar(enderecoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(enderecoDTO);
    }

    @PutMapping("/{idEndereco}")
    @Operation(summary = "Atualiza um Endereco", description = "Atualiza um Endereco")
    @ApiResponse(responseCode = "201", description = "Endereco atualizado com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EnderecoDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<EnderecoDTO> atualizar(@PathVariable Long idEndereco, @RequestBody EnderecoDTO enderecoDTO){
        enderecoDTO = enderecoService.atualizar(idEndereco, enderecoDTO);
        return ResponseEntity.ok(enderecoDTO);
    }

    @DeleteMapping("/{idEndereco}")
    @Operation(summary = "Deletar um Endereco", description = "Deleta um Endereco")
    @ApiResponse(responseCode = "204", description = "Endereco deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Endereco nao encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<Void> deletarPorId(@PathVariable Long idEndereco){
        enderecoService.deletarPorId(idEndereco);
        return ResponseEntity.noContent().build();
    }
}
