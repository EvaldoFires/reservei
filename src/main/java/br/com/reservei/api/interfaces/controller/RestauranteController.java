package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.restaurante.RestauranteService;
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
@RequestMapping("/restaurante")
@RequiredArgsConstructor
public class RestauranteController {

    private final RestauranteService restauranteService;

    @GetMapping("/{idRestaurante}")
    @Operation(summary = "Buscar Restaurante por ID", description = "Busca um Restaurante pelo seu ID")
            @ApiResponse(responseCode = "200", description = "Restaurante encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RestauranteDTO.class)))
            @ApiResponse(responseCode = "404", description = "Restaurante nao encontrado")
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<RestauranteDTO> buscarPorId(@PathVariable Long idRestaurante){
        return ResponseEntity.ok(restauranteService.buscarPorId(idRestaurante));
    }

    @GetMapping
    @Operation(summary = "Buscar todos os Restaurantes", description = "Busca uma lista com todos os Restaurantes")
    @ApiResponse(responseCode = "200", description = "Restaurantes encontrados com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RestauranteDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<List<RestauranteDTO>> buscarTodos(){
        return ResponseEntity.ok(restauranteService.buscarTodos());
    }

    @PostMapping
    @Operation(summary = "Salva um Restaurante", description = "Salva um Restaurante")
    @ApiResponse(responseCode = "201", description = "Restaurante salvo com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RestauranteDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<RestauranteDTO> salvar(@RequestBody RestauranteDTO restauranteDTO){
        restauranteDTO = restauranteService.salvar(restauranteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(restauranteDTO);
    }

    @PutMapping("/{idRestaurante}")
    @Operation(summary = "Atualiza um Restaurante", description = "Atualiza um Restaurante")
    @ApiResponse(responseCode = "201", description = "Restaurante atualizado com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RestauranteDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<RestauranteDTO> atualizar(@PathVariable Long idRestaurante, @RequestBody RestauranteDTO restauranteDTO){
        restauranteDTO = restauranteService.atualizar(idRestaurante, restauranteDTO);
        return ResponseEntity.ok(restauranteDTO);
    }

    @DeleteMapping("/{idRestaurante}")
    @Operation(summary = "Deletar um Restaurante", description = "Deleta um Restaurante")
    @ApiResponse(responseCode = "204", description = "Restaurante deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Restaurante nao encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<Void> deletarPorId(@PathVariable Long idRestaurante){
        restauranteService.deletarPorId(idRestaurante);
        return ResponseEntity.noContent().build();
    }
}
