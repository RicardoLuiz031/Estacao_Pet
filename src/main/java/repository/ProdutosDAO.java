package repository;

import model.Produtos;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutosDAO {

    private Connection conexao;

    public ProdutosDAO() throws SQLException {
        this.conexao = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/usuarios",
                "root",
                ""
        );
    }

    // Cadastrar produto
    public void cadastro(Produtos p) throws SQLException {
        String sql = "INSERT INTO produtos (nome, preco, estoque) VALUES (?, ?, ?)";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, p.GetNome());
        ps.setString(2, p.GetPreco());
        ps.setString(3, p.GetEstoque());
        ps.executeUpdate();
        ps.close();
        System.out.println("Produto cadastrado com sucesso!");
    }

    // Listar todos os produtos
    public List<Produtos> listar() throws SQLException {
        List<Produtos> lista = new ArrayList<>();
        String sql = "SELECT * FROM produtos";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String nome = rs.getString("nome");
            String preco = rs.getString("preco");
            String estoque = rs.getString("estoque");

            Produtos p = new Produtos(nome, preco, estoque);
            lista.add(p);
        }

        rs.close();
        ps.close();
        return lista;
    }

    // Atualizar produto pelo nome
    public void atualizar(Produtos p) throws SQLException {
        String sql = "UPDATE produtos SET preco = ?, estoque = ? WHERE nome = ?";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, p.GetPreco());
        ps.setString(2, p.GetEstoque());
        ps.setString(3, p.GetNome());
        ps.executeUpdate();
        ps.close();
        System.out.println("Produto atualizado com sucesso!");
    }

    // Remover produto pelo nome
    public void remover(String nome) throws SQLException {
        String sql = "DELETE FROM produtos WHERE nome = ?";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, nome);
        ps.executeUpdate();
        ps.close();
        System.out.println("Produto removido com sucesso!");
    }
}