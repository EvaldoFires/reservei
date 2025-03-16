package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.AvaliacaoDTO;
import br.com.reservei.api.application.usecases.avaliacao.AvaliacaoService;
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
@RequestMapping("/avaliacao")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @GetMapping("/{idAvaliacao}")
    @Operation(summary = "Buscar Avaliação por ID", description = "Busca uma Avaliação pelo seu ID")
            @ApiResponse(responseCode = "200", description = "Avaliação encontrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AvaliacaoDTO.class)))
            @ApiResponse(responseCode = "404", description = "Avaliação nao encontrada")
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<AvaliacaoDTO> buscarPorId(@PathVariable Long idAvaliacao){
        return ResponseEntity.ok(avaliacaoService.buscarPorId(idAvaliacao));
    }
    
    @GetMapping
    @Operation(summary = "Buscar todas as Avaliações", description = "Busca uma lista com todas as Avaliações")
    @ApiResponse(responseCode = "200", description = "Avaliações encontradas com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AvaliacaoDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<List<AvaliacaoDTO>> buscarTodos(){
        return ResponseEntity.ok(avaliacaoService.buscarTodos());
    }
    
    @PostMapping
    @Operation(summary = "Salva uma Avaliação", description = "Salva uma Avaliação")
    @ApiResponse(responseCode = "201", description = "Avaliação salva com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AvaliacaoDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<AvaliacaoDTO> salvar(@RequestBody AvaliacaoDTO avaliacaoDTO){
        avaliacaoDTO = avaliacaoService.salvar(avaliacaoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoDTO);
    }
    
    @PutMapping("/{idAvaliacao}")
    @Operation(summary = "Atualiza uma Avaliação", description = "Atualiza uma Avaliação")
    @ApiResponse(responseCode = "201", description = "Avaliação atualizada com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AvaliacaoDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<AvaliacaoDTO> atualizar(@PathVariable Long idAvaliacao, @RequestBody AvaliacaoDTO avaliacaoDTO){
        avaliacaoDTO = avaliacaoService.atualizar(idAvaliacao, avaliacaoDTO);
        return ResponseEntity.ok(avaliacaoDTO);
    }
    
    @DeleteMapping("/{idAvaliacao}")
    @Operation(summary = "Deletar uma Avaliação", description = "Deleta uma Avaliação")
    @ApiResponse(responseCode = "204", description = "Avaliação deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Avaliação nao encontrada")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<Void> deletarPorId(@PathVariable Long idAvaliacao){
        avaliacaoService.deletarPorId(idAvaliacao);
        return ResponseEntity.noContent().build();
    }
}
