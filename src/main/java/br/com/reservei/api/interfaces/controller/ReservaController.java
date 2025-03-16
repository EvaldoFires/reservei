package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.ReservaDTO;
import br.com.reservei.api.application.usecases.reserva.ReservaService;
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
@RequestMapping("/reserva")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @GetMapping("/{idReserva}")
    @Operation(summary = "Buscar Reserva por ID", description = "Busca uma Reserva pelo seu ID")
            @ApiResponse(responseCode = "200", description = "Reserva encontrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservaDTO.class)))
            @ApiResponse(responseCode = "404", description = "Reserva nao encontrada")
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<ReservaDTO> buscarPorId(@PathVariable Long idReserva){
        return ResponseEntity.ok(reservaService.buscarPorId(idReserva));
    }
    
    @GetMapping
    @Operation(summary = "Buscar todas as Reservas", description = "Busca uma lista com todas as Reservas")
    @ApiResponse(responseCode = "200", description = "Reservas encontradas com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ReservaDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<List<ReservaDTO>> buscarTodos(){
        return ResponseEntity.ok(reservaService.buscarTodos());
    }
    
    @PostMapping
    @Operation(summary = "Salva uma Reserva", description = "Salva uma Reserva")
    @ApiResponse(responseCode = "201", description = "Reserva salva com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ReservaDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<ReservaDTO> salvar(@RequestBody ReservaDTO reservaDTO){
        reservaDTO = reservaService.salvar(reservaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaDTO);
    }
    
    @PutMapping("/{idReserva}")
    @Operation(summary = "Atualiza uma Reserva", description = "Atualiza uma Reserva")
    @ApiResponse(responseCode = "201", description = "Reserva atualizada com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ReservaDTO.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @ApiResponse(responseCode = "400", description = "Requisição invalida")
    public ResponseEntity<ReservaDTO> atualizar(@PathVariable Long idReserva, @RequestBody ReservaDTO reservaDTO){
        reservaDTO = reservaService.atualizar(idReserva, reservaDTO);
        return ResponseEntity.ok(reservaDTO);
    }
    
    @DeleteMapping("/{idReserva}")
    @Operation(summary = "Deletar uma Reserva", description = "Deleta uma Reserva")
    @ApiResponse(responseCode = "204", description = "Reserva deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Reserva nao encontrada")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<Void> deletarPorId(@PathVariable Long idReserva){
        reservaService.deletarPorId(idReserva);
        return ResponseEntity.noContent().build();
    }
}
