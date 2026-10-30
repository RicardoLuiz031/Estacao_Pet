package model;

public class Usuarios {
    //Atributos
    private String nome;
    private String login;
    private String senha_hash;


    //construtor
   public Usuarios(String nome,String login,String senha_hash) {
    this.nome = nome;
    this.login = login;
    this.senha_hash = senha_hash;
   }

   //Getters e Setters
public String GetNome() {
    return nome;
}
public void setNome(String nome) {
    this.nome = nome;
}

public String GetLogin() {
    return login;
}
public void setLogin(String login) {
    this.login = login;
}

public String GetSenha_hash() {
    return senha_hash;
}
public void setSenha_hash(String senha_hash) {
    this.senha_hash = senha_hash;
}

//Exibe os dados
@Override
public String toString() {
    return "\n === USUARIOS ===" +
            "\nNome: " + nome +
            "\nLogin: " + login +
            "\nSenha_hash: " + senha_hash;
}
}