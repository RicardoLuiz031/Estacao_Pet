package estacaoPet.controller;

import estacaoPet.Service.AnimaisService;
import estacaoPet.Service.AnimaisService.PetDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500",
        "http://localhost:63342"
}, allowCredentials = "true")
@RestController
@RequestMapping("/api/pets")
public class AnimaisController {

    private final AnimaisService service;

    public AnimaisController(AnimaisService service) {
        this.service = service;
    }

    // GET /api/pets?busca=rex   (nome do pet ou do tutor)
    @GetMapping
    public List<PetDTO> listar(@RequestParam(required = false) String busca) {
        return service.listar(busca);
    }

    @GetMapping("/{id}")
    public PetDTO buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetDTO criar(@Valid @RequestBody PetDTO dados) {
        return service.criar(dados);
    }

    @PutMapping("/{id}")
    public PetDTO atualizar(@PathVariable Integer id, @Valid @RequestBody PetDTO dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id);
    }
}
