package estacaoPet.controller;

import estacaoPet.Secutiry.SessaoConfig;
import estacaoPet.Service.AgendamentosService;
import estacaoPet.Service.AgendamentosService.AgendamentoDTO;
import estacaoPet.Service.AgendamentosService.PainelDTO;
import estacaoPet.Service.AgendamentosService.StatusDTO;
import estacaoPet.model.Agendamentos.StatusAgendamento;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Obs.: o nome da classe/arquivo ficou "Agentamentos" (sem o "d"). Se quiser corrigir,
// renomeie o arquivo e a classe juntos (botão direito > Refactor > Rename no IntelliJ).
@RestController
@RequestMapping("/api")
public class AgentamentosController {

    private final AgendamentosService service;

    public AgentamentosController(AgendamentosService service) {
        this.service = service;
    }

    // GET /api/agendamentos?data=2026-11-03&status=AGENDADO  (os dois filtros são opcionais)
    @GetMapping("/agendamentos")
    public List<AgendamentoDTO> listar(@RequestParam(required = false) String data,
                                       @RequestParam(required = false) StatusAgendamento status) {
        return service.listar(data, status);
    }

    // GET /api/agendamentos/horarios?data=2026-11-03[&ignorarId=5]  ->  ["08:00","08:30",...]
    @GetMapping("/agendamentos/horarios")
    public List<String> horariosLivres(@RequestParam String data,
                                       @RequestParam(required = false) Integer ignorarId) {
        return service.horariosLivres(data, ignorarId);
    }

    @GetMapping("/agendamentos/{id}")
    public AgendamentoDTO buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping("/agendamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoDTO criar(@Valid @RequestBody AgendamentoDTO dados) {
        return service.criar(dados);
    }

    @PutMapping("/agendamentos/{id}")
    public AgendamentoDTO atualizar(@PathVariable Integer id, @Valid @RequestBody AgendamentoDTO dados) {
        return service.atualizar(id, dados);
    }

    // PATCH /api/agendamentos/5/status  { "status": "CONCLUIDO" }
    @PatchMapping("/agendamentos/{id}/status")
    public AgendamentoDTO atualizarStatus(@PathVariable Integer id, @Valid @RequestBody StatusDTO dados) {
        return service.atualizarStatus(id, dados.status());
    }

    @DeleteMapping("/agendamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id);
    }

    // Painel da tela inicial
    @GetMapping("/painel")
    public PainelDTO painel(HttpSession sessao) {
        return service.painel((String) sessao.getAttribute(SessaoConfig.ATTR_NOME));
    }
}
