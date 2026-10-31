package estacaoPet.repository;

import estacaoPet.model.Produtos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutosRepository extends JpaRepository<Produtos, Integer> {

    List<Produtos> findAllByOrderByNomeAsc();

    List<Produtos> findByNomeContainingIgnoreCaseOrderByNome(String nome);
}
