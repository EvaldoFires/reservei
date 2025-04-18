package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.application.usecases.endereco.EstadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estado")
@RequiredArgsConstructor
@Tag(name = "Estado", description = "Recurso para Gestão de Estados")
public class EstadoController {

    private final EstadoService estadoService;

    @GetMapping("/{idEstado}")
    @Operation(summary = "Buscar Estado por ID", description = "Busca um Estado pelo seu ID")
            @ApiResponse(responseCode = "200", description = "Estado encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EstadoDTO.class)))
            @ApiResponse(responseCode = "404", description = "Estado nao encontrado")
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<EstadoDTO> buscarPorId(@PathVariable Long idEstado){
        return ResponseEntity.ok(estadoService.buscarPorId(idEstado));
    }

    @GetMapping
    @Operation(summary = "Buscar todos os Estados", description = "Busca uma lista com todos os Estados")
    @ApiResponse(responseCode = "200", description = "Estados encontrados com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EstadoDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<List<EstadoDTO>> buscarTodos(){
        return ResponseEntity.ok(estadoService.buscarTodos());
    }

    @PostMapping
    @Operation(summary = "Salva um Estado", description = "Salva um Estado")
    @ApiResponse(responseCode = "201", description = "Estado salvo com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EstadoDTO.class)))
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    @ApiResponse(responseCode = "409", description = "Estado já salvo com esses dados")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<EstadoDTO> salvar(@Valid @RequestBody EstadoDTO estadoDTO){
        estadoDTO = estadoService.salvar(estadoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(estadoDTO);
    }

    @PutMapping("/{idEstado}")
    @Operation(summary = "Atualiza um Estado", description = "Atualiza um Estado")
    @ApiResponse(responseCode = "201", description = "Estado atualizado com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EstadoDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<EstadoDTO> atualizar(@PathVariable Long idEstado, @Valid @RequestBody EstadoDTO estadoDTO){
        estadoDTO = estadoService.atualizar(idEstado, estadoDTO);
        return ResponseEntity.ok(estadoDTO);
    }

    @DeleteMapping("/{idEstado}")
    @Operation(summary = "Deletar um Estado", description = "Deleta um Estado")
    @ApiResponse(responseCode = "204", description = "Estado deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Estado nao encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<Void> deletarPorId(@PathVariable Long idEstado){
        estadoService.deletarPorId(idEstado);
        return ResponseEntity.noContent().build();
    }
}
