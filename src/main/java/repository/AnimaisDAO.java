package repository;

import model.Animais;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnimaisDAO {

    private Connection conexao;

    public AnimaisDAO() throws SQLException {
        this.conexao = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/usuarios",
                "root",
                ""
        );
    }

    // Cadastrar animal / pet
    public void cadastro(Animais a) throws SQLException {
        String sql = "INSERT INTO animais (nome, especie, raca, idade) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, a.GetNome());
        ps.setString(2, a.GetEspecie());
        ps.setString(3, a.GetRaca());
        ps.setString(4, a.GetIdade());
        ps.executeUpdate();
        ps.close();
        System.out.println("Animal cadastrado com sucesso!");
    }

    // Listar todos os animais
    public List<Animais> listar() throws SQLException {
        List<Animais> lista = new ArrayList<>();
        String sql = "SELECT * FROM animais";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String nome = rs.getString("nome");
            String especie = rs.getString("especie");
            String raca = rs.getString("raca");
            String idade = rs.getString("idade");

            Animais a = new Animais(nome, especie, raca, idade);
            lista.add(a);
        }

        rs.close();
        ps.close();
        return lista;
    }

    // Atualizar dados do animal pelo nome
    public void atualizar(Animais a) throws SQLException {
        String sql = "UPDATE animais SET especie = ?, raca = ?, idade = ? WHERE nome = ?";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, a.GetEspecie());
        ps.setString(2, a.GetRaca());
        ps.setString(3, a.GetIdade());
        ps.setString(4, a.GetNome());
        ps.executeUpdate();
        ps.close();
        System.out.println("Dados do animal atualizados com sucesso!");
    }

    // Remover animal pelo nome
    public void remover(String nome) throws SQLException {
        String sql = "DELETE FROM animais WHERE nome = ?";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, nome);
        ps.executeUpdate();
        ps.close();
        System.out.println("Animal removido com sucesso!");
    }
}