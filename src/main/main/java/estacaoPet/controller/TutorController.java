package estacaoPet.controller;

import estacaoPet.Service.TutorService;
import estacaoPet.Service.TutorService.TutorDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutores")
public class TutorController {

    private final TutorService service;

    public TutorController(TutorService service) {
        this.service = service;
    }

    // GET /api/tutores?busca=maria   (nome) ou ?busca=12345678909 (CPF exato)
    @GetMapping
    public List<TutorDTO> listar(@RequestParam(required = false) String busca) {
        return service.listar(busca);
    }

    @GetMapping("/{id}")
    public TutorDTO buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TutorDTO criar(@Valid @RequestBody TutorDTO dados) {
        return service.criar(dados);
    }

    @PutMapping("/{id}")
    public TutorDTO atualizar(@PathVariable Integer id, @Valid @RequestBody TutorDTO dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id);
    }
}
