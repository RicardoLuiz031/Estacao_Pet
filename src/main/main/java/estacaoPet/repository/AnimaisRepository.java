package estacaoPet.repository;

import estacaoPet.model.Animais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimaisRepository extends JpaRepository<Animais,Integer> {

    List<Animais> findAllByOrderByNomeAsc();

    // Busca pelo nome do pet OU nome do tutor
    List<Animais> findByNomeContainingIgnoreCaseOrTutorNomeContainingIgnoreCaseOrderByNome(String nomePet, String nomeTutor);

    boolean existsByTutorId(Integer idTutor);
}
