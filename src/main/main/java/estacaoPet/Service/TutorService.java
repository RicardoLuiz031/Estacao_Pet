package estacaoPet.Service;

import estacaoPet.controller.TratadorErros.ErroValidacao;
import estacaoPet.model.Tutor;
import estacaoPet.repository.AnimaisRepository;
import estacaoPet.repository.TutorRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TutorService {

    /**
     * JSON do tutor. O frontend monta "nome" (nome + sobrenome) e "endereco"
     * (rua, número, bairro, cidade/UF, CEP e observação em um único texto).
     */
    public record TutorDTO(
            Integer id,
            @NotBlank(message = "Informe o nome.")
            @Size(max = 200, message = "O nome pode ter no máximo 200 caracteres.") String nome,
            @NotBlank(message = "Informe o telefone.") String telefone,
            @Email(message = "E-mail inválido.")
            @Size(max = 150, message = "O e-mail pode ter no máximo 150 caracteres.") String email,
            @NotBlank(message = "Informe o CPF.") String cpf,
            @Size(max = 300, message = "O endereço pode ter no máximo 300 caracteres.") String endereco) {
    }

    private final TutorRepository repo;
    private final AnimaisRepository animaisRepo;

    public TutorService(TutorRepository repo, AnimaisRepository animaisRepo) {
        // Se a variável ESTACAO_CHAVE não existir, a aplicação falha já na subida
        Tutor.Cripto.validarChave();
        this.repo = repo;
        this.animaisRepo = animaisRepo;
    }

    @Transactional(readOnly = true)
    public List<TutorDTO> listar(String busca) {
        String termo = busca == null ? "" : busca.trim();
        List<Tutor> tutores;
        if (termo.isEmpty()) {
            tutores = repo.findAllByOrderByNomeAsc();
        } else {
            String digitos = somenteDigitos(termo);
            if (digitos.length() == 11 && termo.matches("[\\d.\\-\\s]+")) {
                // Parece um CPF: CPF está criptografado, então a busca é exata, pelo hash
                tutores = repo.findByCpfHash(Tutor.Cripto.hashBusca(digitos)).map(List::of).orElse(List.of());
            } else {
                tutores = repo.findByNomeContainingIgnoreCaseOrderByNome(termo);
            }
        }
        return tutores.stream().map(this::paraDTO).toList();
    }

    @Transactional(readOnly = true)
    public TutorDTO buscar(Integer id) {
        return paraDTO(obter(id));
    }

    @Transactional
    public TutorDTO criar(TutorDTO dados) {
        Tutor tutor = new Tutor();
        aplicar(tutor, dados);
        return paraDTO(repo.save(tutor));
    }

    @Transactional
    public TutorDTO atualizar(Integer id, TutorDTO dados) {
        Tutor tutor = obter(id);
        aplicar(tutor, dados);
        return paraDTO(repo.save(tutor));
    }

    @Transactional
    public void excluir(Integer id) {
        obter(id);
        if (animaisRepo.existsByTutorId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este tutor possui pets cadastrados. Exclua os pets antes de excluir o tutor.");
        }
        repo.deleteById(id);
    }

    public long total() {
        return repo.count();
    }

    // ---------- Regras e validações ----------
    private void aplicar(Tutor tutor, TutorDTO dados) {
        Map<String, String> erros = new LinkedHashMap<>();
        String cpf = somenteDigitos(dados.cpf());
        String telefone = somenteDigitos(dados.telefone());
        if (!cpfValido(cpf)) {
            erros.put("cpf", "CPF inválido. Informe os 11 números.");
        }
        if (telefone.length() < 10 || telefone.length() > 11) {
            erros.put("telefone", "Telefone inválido. Use DDD + número (10 ou 11 dígitos).");
        }
        if (!erros.isEmpty()) {
            throw new ErroValidacao(erros);
        }

        String hash = Tutor.Cripto.hashBusca(cpf);
        boolean duplicado = tutor.getId() == null
                ? repo.existsByCpfHash(hash)
                : repo.existsByCpfHashAndIdNot(hash, tutor.getId());
        if (duplicado) {
            throw new ErroValidacao("cpf", "Já existe um tutor cadastrado com este CPF.");
        }

        tutor.setNome(dados.nome().trim());
        tutor.setCpf(cpf);
        tutor.setCpfHash(hash);
        tutor.setTelefone(telefone);
        tutor.setEmail(vazioParaNulo(dados.email(), true));
        tutor.setEndereco(vazioParaNulo(dados.endereco(), false));
    }

    private Tutor obter(Integer id) {
        return repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutor não encontrado."));
    }

    private TutorDTO paraDTO(Tutor t) {
        return new TutorDTO(t.getId(), t.getNome(), t.getTelefone(), t.getEmail(), t.getCpf(), t.getEndereco());
    }

    private static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    private static String vazioParaNulo(String texto, boolean minusculo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return minusculo ? texto.trim().toLowerCase() : texto.trim();
    }

    /** Valida os dois dígitos verificadores do CPF. */
    static boolean cpfValido(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digito(cpf, 9, 10) == cpf.charAt(9) - '0'
                && digito(cpf, 10, 11) == cpf.charAt(10) - '0';
    }

    private static int digito(String cpf, int quantidade, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
