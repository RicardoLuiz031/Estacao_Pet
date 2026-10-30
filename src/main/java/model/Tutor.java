package model;

public class Tutor {
    //Atributos
    private String nome;
    private String cpf_cif;
    private String telefone_cif;
    private String email_cif;
    private String endereco_cif;


    //construtor
    public Tutor(String nome,String cpf_cif,String telefone_cif,
                        String email_cif,String endereco_cif) {
        this.nome = nome;
        this.cpf_cif = cpf_cif;
        this.telefone_cif = telefone_cif;
        this.email_cif = email_cif;
        this.endereco_cif = endereco_cif;
    }

    //Getters e Setters
    public String GetNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String GetCpf_cif() {
        return cpf_cif;
    }
    public void setTelefone_cif(String telefone_cif) {
        this.telefone_cif = telefone_cif;
    }

    public String GetEmail_cif() {
        return email_cif;
    }
    public void setEmail_cif(String email_cif) {
        this.email_cif = email_cif;
    }

    public String GetEndereco_cif() {
        return endereco_cif;
    }
    public void setEndereco_cif(String endereco_cif) {
        this.endereco_cif = endereco_cif;
    }

    //Exibe os dados
    @Override
    public String toString() {
        return "\n === TUTOR ===" +
                "\nNome: " + nome +
                "\nLogin: " + cpf_cif +
                "\nTelefone: " + telefone_cif +
                "\nEmail: " + email_cif +
                "\nEndereco: " + endereco_cif;

    }
}