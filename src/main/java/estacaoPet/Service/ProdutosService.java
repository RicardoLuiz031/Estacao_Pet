package estacaoPet.Service;

import estacaoPet.model.Produtos;
import estacaoPet.repository.ProdutosRepository;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProdutosService {

    public record ProdutoDTO(
            Integer id,
            @NotBlank(message = "Informe o nome do produto.")
            @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.") String nome,
            @NotNull(message = "Informe o preço.")
            @DecimalMin(value = "0.00", message = "O preço não pode ser negativo.")
            @DecimalMax(value = "999999.99", message = "O preço máximo é 999999,99.") BigDecimal preco,
            @NotNull(message = "Informe o estoque.")
            @Min(value = 0, message = "O estoque não pode ser negativo.") Integer estoque) {
    }

    private final ProdutosRepository repo;

    public ProdutosService(ProdutosRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ProdutoDTO> listar(String busca) {
        List<Produtos> lista = (busca == null || busca.isBlank())
                ? repo.findAllByOrderByNomeAsc()
                : repo.findByNomeContainingIgnoreCaseOrderByNome(busca.trim());
        return lista.stream().map(this::paraDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoDTO buscar(Integer id) {
        return paraDTO(obter(id));
    }

    @Transactional
    public ProdutoDTO criar(ProdutoDTO dados) {
        Produtos produto = new Produtos();
        aplicar(produto, dados);
        return paraDTO(repo.save(produto));
    }

    @Transactional
    public ProdutoDTO atualizar(Integer id, ProdutoDTO dados) {
        Produtos produto = obter(id);
        aplicar(produto, dados);
        return paraDTO(repo.save(produto));
    }

    @Transactional
    public void excluir(Integer id) {
        obter(id);
        repo.deleteById(id);
    }

    private void aplicar(Produtos produto, ProdutoDTO dados) {
        produto.setNome(dados.nome().trim());
        produto.setPreco(dados.preco().setScale(2, RoundingMode.HALF_UP));
        produto.setEstoque(dados.estoque());
    }

    private Produtos obter(Integer id) {
        return repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
    }

    private ProdutoDTO paraDTO(Produtos p) {
        return new ProdutoDTO(p.getId(), p.getNome(), p.getPreco(), p.getEstoque());
    }
}
