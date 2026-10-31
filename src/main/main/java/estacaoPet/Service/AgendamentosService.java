package estacaoPet.Service;

import estacaoPet.controller.TratadorErros.ErroValidacao;
import estacaoPet.model.Agendamentos;
import estacaoPet.model.Agendamentos.Servico;
import estacaoPet.model.Agendamentos.StatusAgendamento;
import estacaoPet.model.Animais;
import estacaoPet.repository.AgendamentosRepository;
import estacaoPet.repository.AnimaisRepository;
import estacaoPet.repository.TutorRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AgendamentosService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    // Expediente: 08:00 às 17:30, um horário a cada 30 minutos
    private static final LocalTime PRIMEIRO_HORARIO = LocalTime.of(8, 0);
    private static final LocalTime ULTIMO_HORARIO = LocalTime.of(17, 30);
    private static final int PASSO_MINUTOS = 30;

    /** "data" no formato yyyy-MM-dd e "horario" no formato HH:mm (é o que o input date/select do HTML usa). */
    public record AgendamentoDTO(
            Integer id,
            @NotNull(message = "Selecione o pet.") Integer petId,
            String petNome,
            String tutorNome,
            @NotNull(message = "Selecione o serviço.") Servico servico,
            @NotBlank(message = "Informe a data.") String data,
            @NotBlank(message = "Selecione o horário.") String horario,
            StatusAgendamento status) {
    }

    public record StatusDTO(@NotNull(message = "Informe o status.") StatusAgendamento status) {
    }

    public record PainelDTO(String nome, long totalAgendamentosHoje, long totalClientes,
                            List<AgendamentoDTO> agendamentosHoje) {
    }

    private final AgendamentosRepository repo;
    private final AnimaisRepository animaisRepo;
    private final TutorRepository tutorRepo;

    public AgendamentosService(AgendamentosRepository repo, AnimaisRepository animaisRepo, TutorRepository tutorRepo) {
        this.repo = repo;
        this.animaisRepo = animaisRepo;
        this.tutorRepo = tutorRepo;
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listar(String data, StatusAgendamento status) {
        LocalDateTime inicio = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2999, 12, 31, 23, 59, 59);
        if (data != null && !data.isBlank()) {
            LocalDate dia = lerData(data);
            inicio = dia.atStartOfDay();
            fim = dia.atTime(23, 59, 59);
        }
        List<Agendamentos> lista = (status == null)
                ? repo.findByDataHoraBetweenOrderByDataHoraAsc(inicio, fim)
                : repo.findByDataHoraBetweenAndStatusOrderByDataHoraAsc(inicio, fim, status);
        return lista.stream().map(this::paraDTO).toList();
    }

    @Transactional(readOnly = true)
    public AgendamentoDTO buscar(Integer id) {
        return paraDTO(obter(id));
    }

    /** Horários livres de um dia (alimenta o select do formulário). ignorarId = agendamento sendo editado. */
    @Transactional(readOnly = true)
    public List<String> horariosLivres(String data, Integer ignorarId) {
        LocalDate dia = lerData(data);
        LocalDateTime agora = LocalDateTime.now(FUSO);
        if (dia.isBefore(agora.toLocalDate())) {
            return List.of();
        }
        Set<LocalTime> ocupados = repo.findByDataHoraBetweenOrderByDataHoraAsc(dia.atStartOfDay(), dia.atTime(23, 59, 59))
                .stream()
                .filter(a -> ignorarId == null || !ignorarId.equals(a.getId()))
                .map(a -> a.getDataHora().toLocalTime())
                .collect(Collectors.toSet());
        List<String> livres = new ArrayList<>();
        for (LocalTime horario : horariosDoDia()) {
            boolean passou = dia.equals(agora.toLocalDate()) && !horario.isAfter(agora.toLocalTime());
            if (!ocupados.contains(horario) && !passou) {
                livres.add(horario.toString());
            }
        }
        return livres;
    }

    @Transactional
    public AgendamentoDTO criar(AgendamentoDTO dados) {
        Agendamentos agendamento = new Agendamentos();
        preencher(agendamento, dados);
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        return paraDTO(gravar(agendamento));
    }

    @Transactional
    public AgendamentoDTO atualizar(Integer id, AgendamentoDTO dados) {
        Agendamentos agendamento = obter(id);
        preencher(agendamento, dados);
        if (dados.status() != null) {
            agendamento.setStatus(dados.status());
        }
        return paraDTO(gravar(agendamento));
    }

    @Transactional
    public AgendamentoDTO atualizarStatus(Integer id, StatusAgendamento status) {
        Agendamentos agendamento = obter(id);
        agendamento.setStatus(status);
        return paraDTO(repo.save(agendamento));
    }

    @Transactional
    public void excluir(Integer id) {
        obter(id);
        repo.deleteById(id);
    }

    /** Resumo da tela inicial: agendamentos de hoje (sem cancelados) e total de clientes (tutores). */
    @Transactional(readOnly = true)
    public PainelDTO painel(String nomeUsuario) {
        LocalDate hoje = LocalDate.now(FUSO);
        List<AgendamentoDTO> deHoje = repo
                .findByDataHoraBetweenOrderByDataHoraAsc(hoje.atStartOfDay(), hoje.atTime(23, 59, 59))
                .stream().map(this::paraDTO).toList();
        long ativos = deHoje.stream().filter(a -> a.status() != StatusAgendamento.CANCELADO).count();
        return new PainelDTO(nomeUsuario, ativos, tutorRepo.count(), deHoje);
    }

    // ---------- Regras de negócio ----------
    private void preencher(Agendamentos agendamento, AgendamentoDTO dados) {
        LocalDate dia = lerData(dados.data());
        LocalTime hora = lerHora(dados.horario());
        Animais pet = animaisRepo.findById(dados.petId())
                .orElseThrow(() -> new ErroValidacao("pet", "Pet não encontrado."));

        if (!horariosDoDia().contains(hora)) {
            throw new ErroValidacao("horario", "Horário fora do expediente (08:00 às 17:30, de 30 em 30 minutos).");
        }
        LocalDateTime dataHora = dia.atTime(hora);

        boolean mudouHorario = agendamento.getId() == null || !dataHora.equals(agendamento.getDataHora());
        if (mudouHorario) {
            if (dataHora.isBefore(LocalDateTime.now(FUSO))) {
                throw new ErroValidacao("data", "Não é possível agendar em uma data/horário que já passou.");
            }
            if (repo.existsByDataHora(dataHora)) {
                throw new ErroValidacao("horario", "Este horário já está ocupado. Escolha outro.");
            }
        }
        agendamento.setAnimais(pet);
        agendamento.setServico(dados.servico());
        agendamento.setDataHora(dataHora);
    }

    private Agendamentos gravar(Agendamentos agendamento) {
        try {
            // flush imediato: se duas pessoas tentarem o mesmo horário ao mesmo tempo, o UNIQUE do banco barra
            return repo.saveAndFlush(agendamento);
        } catch (DataIntegrityViolationException e) {
            throw new ErroValidacao("horario", "Este horário já está ocupado. Escolha outro.");
        }
    }

    private List<LocalTime> horariosDoDia() {
        List<LocalTime> horarios = new ArrayList<>();
        for (LocalTime h = PRIMEIRO_HORARIO; !h.isAfter(ULTIMO_HORARIO); h = h.plusMinutes(PASSO_MINUTOS)) {
            horarios.add(h);
        }
        return horarios;
    }

    private LocalDate lerData(String texto) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ErroValidacao("data", "Data inválida.");
        }
    }

    private LocalTime lerHora(String texto) {
        try {
            return LocalTime.parse(texto.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ErroValidacao("horario", "Horário inválido.");
        }
    }

    private Agendamentos obter(Integer id) {
        return repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado."));
    }

    private AgendamentoDTO paraDTO(Agendamentos a) {
        return new AgendamentoDTO(
                a.getId(),
                a.getAnimais().getId(),
                a.getAnimais().getNome(),
                a.getAnimais().getTutor().getNome(),
                a.getServico(),
                a.getDataHora().toLocalDate().toString(),
                a.getDataHora().toLocalTime().toString(),
                a.getStatus());
    }
}
