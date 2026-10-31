package estacaoPet.repository;

import estacaoPet.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Integer> {

    List<Tutor> findAllByOrderByNomeAsc();

    // Só o nome (não criptografado) pode usar LIKE
    List<Tutor> findByNomeContainingIgnoreCaseOrderByNome(String nome);

    // CPF é buscado pelo hash (o CPF em si está criptografado no banco)
    Optional<Tutor> findByCpfHash(String cpfHash);

    boolean existsByCpfHash(String cpfHash);

    boolean existsByCpfHashAndIdNot(String cpfHash, Integer id);
}
