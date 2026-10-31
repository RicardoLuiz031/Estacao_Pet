package estacaoPet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "animais")
public class Animais {
    //Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_animais")
    private Integer id;
    @Column(name = "nomeAnimal")
    private String nome;
    @Column(name = "especieAnimal")
    private String especie;
    @Column(name = "racaAnimal")
    private String raca;
    @Column(name = "idadeAnimal")
    private String idade;

    @ManyToOne
    @JoinColumn(name = "id_tutor")
    private Tutor tutor;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getIdade() {
        return idade;
    }

    public void setIdade(String idade) {
        this.idade = idade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }
}