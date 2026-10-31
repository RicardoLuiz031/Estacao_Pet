package estacaoPet.Service;

import estacaoPet.controller.TratadorErros.ErroValidacao;
import estacaoPet.model.Usuarios;
import estacaoPet.model.Usuarios.Perfil;
import estacaoPet.repository.UsuarioRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    // ---------- DTOs (formato do JSON trocado com o frontend) ----------
    public record LoginDTO(
            @NotBlank(message = "Informe o e-mail.") String login,
            @NotBlank(message = "Informe a senha.") String senha) {
    }

    public record FuncionarioDTO(Integer id, String nome, String login, String perfil) {
    }

    public record NovoFuncionarioDTO(
            String nome,
            @NotBlank(message = "Informe o e-mail.")
            @Email(message = "E-mail inválido.")
            @Size(max = 50, message = "O e-mail pode ter no máximo 50 caracteres.") String login,
            @NotBlank(message = "Informe a senha.")
            @Size(min = 6, max = 72, message = "A senha deve ter de 6 a 72 caracteres.") String senha,
            Perfil perfil) {
    }

    public record SenhaDTO(
            @NotBlank(message = "Informe a senha atual.") String senhaAtual,
            @NotBlank(message = "Informe a nova senha.")
            @Size(min = 6, max = 72, message = "A senha deve ter de 6 a 72 caracteres.") String senhaNova) {
    }

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final String adminNome;
    private final String adminLogin;
    private final String adminSenha;
    // Hash "falso": usado quando o usuário não existe, para o tempo de resposta não revelar quais e-mails existem
    private final String hashFalso;

    public UsuarioService(UsuarioRepository repo,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.admin.nome:Administrador}") String adminNome,
                          @Value("${app.admin.login:}") String adminLogin,
                          @Value("${app.admin.senha:}") String adminSenha) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.adminNome = adminNome;
        this.adminLogin = adminLogin;
        this.adminSenha = adminSenha;
        this.hashFalso = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    // ---------- Cadastro inicial do administrador ----------
    @Override
    public void run(ApplicationArguments args) {
        if (repo.count() > 0) {
            return;
        }
        if (adminLogin.isBlank() || adminSenha.length() < 6) {
            log.warn("Nenhum usuário cadastrado. Defina as variáveis de ambiente ADMIN_LOGIN e ADMIN_SENHA "
                    + "(mínimo 6 caracteres) e reinicie para criar o administrador inicial.");
            return;
        }
        salvar(adminNome, adminLogin, adminSenha, Perfil.ADMIN);
        log.info("Administrador inicial criado.");
    }

    // ---------- Login ----------
    @Transactional(readOnly = true)
    public Optional<Usuarios> autenticar(String login, String senha) {
        Optional<Usuarios> usuario = repo.findByLogin(normalizar(login));
        String hash = usuario.map(Usuarios::getSenhaHash).orElse(hashFalso);
        boolean confere = passwordEncoder.matches(senha, hash);
        return usuario.filter(u -> confere);
    }

    // ---------- Perfil do usuário logado ----------
    @Transactional(readOnly = true)
    public FuncionarioDTO buscar(Integer id) {
        return paraDTO(repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado.")));
    }

    @Transactional
    public void alterarSenha(Integer id, SenhaDTO dados) {
        Usuarios usuario = repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));
        if (!passwordEncoder.matches(dados.senhaAtual(), usuario.getSenhaHash())) {
            throw new ErroValidacao("senhaAtual", "Senha atual incorreta.");
        }
        if (passwordEncoder.matches(dados.senhaNova(), usuario.getSenhaHash())) {
            throw new ErroValidacao("senhaNova", "A nova senha deve ser diferente da atual.");
        }
        usuario.setSenhaHash(passwordEncoder.encode(dados.senhaNova()));
        repo.save(usuario);
    }

    // ---------- Funcionários (somente ADMIN) ----------
    @Transactional(readOnly = true)
    public List<FuncionarioDTO> listarFuncionarios(String busca) {
        List<Usuarios> lista = (busca == null || busca.isBlank())
                ? repo.findAllByOrderByLoginAsc()
                : repo.findByLoginContainingIgnoreCaseOrderByLogin(busca.trim());
        return lista.stream().map(this::paraDTO).toList();
    }

    @Transactional
    public FuncionarioDTO criarFuncionario(NovoFuncionarioDTO dados) {
        String login = normalizar(dados.login());
        if (repo.existsByLogin(login)) {
            throw new ErroValidacao("login", "Já existe um usuário com este e-mail.");
        }
        String nome = (dados.nome() == null || dados.nome().isBlank())
                ? login.substring(0, login.indexOf('@'))
                : dados.nome().trim();
        Perfil perfil = dados.perfil() == null ? Perfil.ATENDENTE : dados.perfil();
        return paraDTO(salvar(nome, login, dados.senha(), perfil));
    }

    @Transactional
    public void excluirFuncionario(Integer id, Integer idLogado) {
        if (id.equals(idLogado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você não pode excluir o seu próprio usuário.");
        }
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado.");
        }
        repo.deleteById(id);
    }

    // ---------- Auxiliares ----------
    private Usuarios salvar(String nome, String login, String senhaPura, Perfil perfil) {
        Usuarios u = new Usuarios();
        u.setNome(nome);
        u.setLogin(normalizar(login));
        u.setSenhaHash(passwordEncoder.encode(senhaPura)); // BCrypt: hash com sal, nunca texto puro
        u.setPerfil(perfil);
        return repo.save(u);
    }

    private String normalizar(String login) {
        return login == null ? "" : login.trim().toLowerCase();
    }

    private FuncionarioDTO paraDTO(Usuarios u) {
        return new FuncionarioDTO(u.getId(), u.getNome(), u.getLogin(), u.getPerfil().name());
    }
}
