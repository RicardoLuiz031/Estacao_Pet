package model;

public class Animais {
    //Atributos
    private String nome;
    private String especie;
    private String raca;
    private String idade;


    //construtor
    public Animais(String nome,String especie,String raca,
                 String idade) {
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.idade = idade;
    }

    //Getters e Setters
    public String GetNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String GetEspecie() {
        return especie;
    }
    public void setEspecie(String Especie) {
        this.especie = especie;
    }

    public String GetRaca() {
        return raca;
    }
    public void setRaca(String raca) {
        this.raca = raca;
    }

    public String GetIdade() {
        return idade;
    }
    public void setidade(String idade) {
        this.idade = idade;
    }

    //Exibe os dados
    @Override
    public String toString() {
        return "\n === ANIMAIS ===" +
                "\nNome: " + nome +
                "\nEspecie: " + especie +
                "\nRaca: " + raca +
                "\nIdade: " + idade;

    }
}