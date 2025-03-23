package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.CidadeDTO;
import br.com.reservei.api.application.usecases.endereco.CidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cidade")
@RequiredArgsConstructor
@Tag(name = "Cidade", description = "Recurso para Gestão de Cidades")
public class CidadeController {

    private final CidadeService cidadeService;

    @GetMapping("/{idCidade}")
    @Operation(summary = "Buscar Cidade por ID", description = "Busca uma Cidade pelo seu ID")
            @ApiResponse(responseCode = "200", description = "Cidade encontrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CidadeDTO.class)))
            @ApiResponse(responseCode = "404", description = "Cidade nao encontrada")
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<CidadeDTO> buscarPorId(@PathVariable Long idCidade){
        return ResponseEntity.ok(cidadeService.buscarPorId(idCidade));
    }
    
    @GetMapping
    @Operation(summary = "Buscar todas as Cidades", description = "Busca uma lista com todas as Cidades")
    @ApiResponse(responseCode = "200", description = "Cidades encontradas com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CidadeDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<List<CidadeDTO>> buscarTodos(){
        return ResponseEntity.ok(cidadeService.buscarTodos());
    }
    
    @PostMapping
    @Operation(summary = "Salva uma Cidade", description = "Salva uma Cidade")
    @ApiResponse(responseCode = "201", description = "Cidade salva com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CidadeDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<CidadeDTO> salvar(@RequestBody CidadeDTO cidadeDTO){
        cidadeDTO = cidadeService.salvar(cidadeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cidadeDTO);
    }
    
    @PutMapping("/{idCidade}")
    @Operation(summary = "Atualiza uma Cidade", description = "Atualiza uma Cidade")
    @ApiResponse(responseCode = "201", description = "Cidade atualizada com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CidadeDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<CidadeDTO> atualizar(@PathVariable Long idCidade, @RequestBody CidadeDTO cidadeDTO){
        cidadeDTO = cidadeService.atualizar(idCidade, cidadeDTO);
        return ResponseEntity.ok(cidadeDTO);
    }
    
    @DeleteMapping("/{idCidade}")
    @Operation(summary = "Deletar uma Cidade", description = "Deleta uma Cidade")
    @ApiResponse(responseCode = "204", description = "Cidade deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Cidade nao encontrada")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<Void> deletarPorId(@PathVariable Long idCidade){
        cidadeService.deletarPorId(idCidade);
        return ResponseEntity.noContent().build();
    }
}
