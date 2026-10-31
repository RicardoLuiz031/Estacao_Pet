package estacaoPet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuarios {

    // Perfis de acesso (o frontend usa "ADMIN" para liberar a tela de funcionários)
    public enum Perfil {
        ADMIN,
        ATENDENTE
    }

    //Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuarios")
    private Integer id;
    @Column(name = "nomeUsuario")
    private String nome;
    @Column(name = "loginUsuario")
    private String login;
    @Column(name = "senhaHashUsuario")
    private String senhaHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "perfilUsuario", nullable = false)
    private Perfil perfil = Perfil.ATENDENTE;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }
}
