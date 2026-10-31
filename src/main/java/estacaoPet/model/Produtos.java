package estacaoPet.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
public class Produtos {
    //Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProduto")
    private Integer id;
    @Column(name = "nomeProduto")
    private String nome;
    // BigDecimal: dinheiro não deve usar Double (a coluna é decimal(8,2))
    @Column(name = "precoProduto", precision = 8, scale = 2)
    private BigDecimal preco;
    @Column(name = "estoqueProduto")
    private Integer estoque;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }
}
