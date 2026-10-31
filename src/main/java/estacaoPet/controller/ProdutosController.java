package estacaoPet.controller;

import estacaoPet.Service.ProdutosService;
import estacaoPet.Service.ProdutosService.ProdutoDTO;
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
@RequestMapping("/api/produtos")
public class ProdutosController {

    private final ProdutosService service;

    public ProdutosController(ProdutosService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProdutoDTO> listar(@RequestParam(required = false) String busca) {
        return service.listar(busca);
    }

    @GetMapping("/{id}")
    public ProdutoDTO buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoDTO criar(@Valid @RequestBody ProdutoDTO dados) {
        return service.criar(dados);
    }

    @PutMapping("/{id}")
    public ProdutoDTO atualizar(@PathVariable Integer id, @Valid @RequestBody ProdutoDTO dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id);
    }
}
