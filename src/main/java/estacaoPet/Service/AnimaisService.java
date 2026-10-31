package estacaoPet.Service;

import estacaoPet.controller.TratadorErros.ErroValidacao;
import estacaoPet.model.Animais;
import estacaoPet.model.Tutor;
import estacaoPet.repository.AgendamentosRepository;
import estacaoPet.repository.AnimaisRepository;
import estacaoPet.repository.TutorRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AnimaisService {

    /** JSON do pet. "tutorNome" só é preenchido na resposta (para a listagem). */
    public record PetDTO(
            Integer id,
            @NotBlank(message = "Informe o nome do pet.")
            @Size(max = 200, message = "O nome pode ter no máximo 200 caracteres.") String nome,
            @NotBlank(message = "Informe a espécie.")
            @Size(max = 150, message = "A espécie pode ter no máximo 150 caracteres.") String especie,
            @NotBlank(message = "Informe a raça.")
            @Size(max = 100, message = "A raça pode ter no máximo 100 caracteres.") String raca,
            @NotBlank(message = "Informe a idade.")
            @Size(max = 150, message = "A idade pode ter no máximo 150 caracteres.") String idade,
            @NotNull(message = "Selecione o tutor.") Integer tutorId,
            String tutorNome) {
    }

    private final AnimaisRepository repo;
    private final TutorRepository tutorRepo;
    private final AgendamentosRepository agendamentosRepo;

    public AnimaisService(AnimaisRepository repo, TutorRepository tutorRepo, AgendamentosRepository agendamentosRepo) {
        this.repo = repo;
        this.tutorRepo = tutorRepo;
        this.agendamentosRepo = agendamentosRepo;
    }

    @Transactional(readOnly = true)
    public List<PetDTO> listar(String busca) {
        List<Animais> pets;
        if (busca == null || busca.isBlank()) {
            pets = repo.findAllByOrderByNomeAsc();
        } else {
            String termo = busca.trim();
            pets = repo.findByNomeContainingIgnoreCaseOrTutorNomeContainingIgnoreCaseOrderByNome(termo, termo);
        }
        return pets.stream().map(this::paraDTO).toList();
    }

    @Transactional(readOnly = true)
    public PetDTO buscar(Integer id) {
        return paraDTO(obter(id));
    }

    @Transactional
    public PetDTO criar(PetDTO dados) {
        Animais pet = new Animais();
        aplicar(pet, dados);
        return paraDTO(repo.save(pet));
    }

    @Transactional
    public PetDTO atualizar(Integer id, PetDTO dados) {
        Animais pet = obter(id);
        aplicar(pet, dados);
        return paraDTO(repo.save(pet));
    }

    @Transactional
    public void excluir(Integer id) {
        obter(id);
        if (agendamentosRepo.existsByAnimaisId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este pet possui agendamentos. Exclua os agendamentos antes de excluir o pet.");
        }
        repo.deleteById(id);
    }

    private void aplicar(Animais pet, PetDTO dados) {
        Tutor tutor = tutorRepo.findById(dados.tutorId())
                .orElseThrow(() -> new ErroValidacao("tutor", "Tutor não encontrado."));
        pet.setNome(dados.nome().trim());
        pet.setEspecie(dados.especie().trim());
        pet.setRaca(dados.raca().trim());
        pet.setIdade(dados.idade().trim());
        pet.setTutor(tutor);
    }

    private Animais obter(Integer id) {
        return repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet não encontrado."));
    }

    private PetDTO paraDTO(Animais a) {
        return new PetDTO(a.getId(), a.getNome(), a.getEspecie(), a.getRaca(), a.getIdade(),
                a.getTutor().getId(), a.getTutor().getNome());
    }
}
