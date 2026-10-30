package model;

public class Produtos {
    //Atributos
    private String nome;
    private String preco;
    private String estoque;
   

    //construtor
    public Produtos(String nome,String preco,String estoque) {
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
    }

    //Getters e Setters
    public String GetNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String GetPreco() {
        return preco;
    }
    public void setPreco(String preco) {
        this.preco = preco;
    }

    public String GetEstoque() {
        return estoque;
    }
    public void setEstoque(String estoque) {
        this.estoque = estoque;
    }


    //Exibe os dados
    @Override
    public String toString() {
        return "\n === PRODUTOS ===" +
                "\nNome: " + nome +
                "\nPreco: " + preco +
                "\nEstoque: " + estoque;
    }
}