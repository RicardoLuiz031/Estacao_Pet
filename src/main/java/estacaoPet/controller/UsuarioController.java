package estacaoPet.controller;

import estacaoPet.Secutiry.SessaoConfig;
import estacaoPet.Service.UsuarioService;
import estacaoPet.Service.UsuarioService.FuncionarioDTO;
import estacaoPet.Service.UsuarioService.LoginDTO;
import estacaoPet.Service.UsuarioService.NovoFuncionarioDTO;
import estacaoPet.Service.UsuarioService.SenhaDTO;
import estacaoPet.model.Usuarios;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500",
        "http://localhost:63342"
}, allowCredentials = "true")
@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // POST /api/login  { "login": "email", "senha": "..." }  ->  { "nome": "...", "perfil": "ADMIN" }
    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginDTO dados, HttpServletRequest request) {
        Usuarios usuario = service.autenticar(dados.login(), dados.senha())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos."));

        // Descarta a sessão anterior e cria uma nova (evita fixação de sessão)
        HttpSession antiga = request.getSession(false);
        if (antiga != null) {
            antiga.invalidate();
        }
        HttpSession sessao = request.getSession(true);
        sessao.setMaxInactiveInterval(120); // trava de segurança; a regra dos 30 s está no SessaoConfig
        sessao.setAttribute(SessaoConfig.ATTR_ID, usuario.getId());
        sessao.setAttribute(SessaoConfig.ATTR_NOME, usuario.getNome());
        sessao.setAttribute(SessaoConfig.ATTR_PERFIL, usuario.getPerfil().name());
        sessao.setAttribute(SessaoConfig.ATTR_ULTIMA, System.currentTimeMillis());

        return Map.of("nome", usuario.getNome(), "perfil", usuario.getPerfil().name());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
    }

    // Chamada pelo inatividade.js: o simples fato de passar pelo interceptor renova a sessão
    @GetMapping("/sessao/ping")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ping() {
    }

    // Dados do usuário logado (tela de perfil)
    @GetMapping("/me")
    public FuncionarioDTO me(HttpSession sessao) {
        return service.buscar(idLogado(sessao));
    }

    @PutMapping("/me/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(HttpSession sessao, @Valid @RequestBody SenhaDTO dados) {
        service.alterarSenha(idLogado(sessao), dados);
    }

    // ---------- Funcionários (somente ADMIN: o SessaoConfig bloqueia os demais com 403) ----------
    @GetMapping("/funcionarios")
    public List<FuncionarioDTO> listarFuncionarios(@RequestParam(required = false) String busca) {
        return service.listarFuncionarios(busca);
    }

    @PostMapping("/funcionarios")
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioDTO criarFuncionario(@Valid @RequestBody NovoFuncionarioDTO dados) {
        return service.criarFuncionario(dados);
    }

    @DeleteMapping("/funcionarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirFuncionario(@PathVariable Integer id, HttpSession sessao) {
        service.excluirFuncionario(id, idLogado(sessao));
    }

    private Integer idLogado(HttpSession sessao) {
        return (Integer) sessao.getAttribute(SessaoConfig.ATTR_ID);
    }
}
