package estacaoPet.repository;

import estacaoPet.model.Agendamentos;
import estacaoPet.model.Agendamentos.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentosRepository extends JpaRepository<Agendamentos, Integer> {

    boolean existsByDataHora(LocalDateTime dataHora);

    boolean existsByAnimaisId(Integer idAnimal);

    List<Agendamentos> findByDataHoraBetweenOrderByDataHoraAsc(LocalDateTime inicio, LocalDateTime fim);

    List<Agendamentos> findByDataHoraBetweenAndStatusOrderByDataHoraAsc(LocalDateTime inicio, LocalDateTime fim, StatusAgendamento status);
}
