package estacaoPet.repository;

import estacaoPet.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuarios, Integer> {

    // O Spring monta a consulta pelo NOME DO ATRIBUTO da entidade (login), não pelo nome da coluna
    Optional<Usuarios> findByLogin(String login);

    boolean existsByLogin(String login);

    List<Usuarios> findByLoginContainingIgnoreCaseOrderByLogin(String trecho);

    List<Usuarios> findAllByOrderByLoginAsc();
}
