package repository;

import model.Usuarios;
import java.sql.*;

public class UsuariosDAO {

    private Connection conexao;

    public UsuariosDAO() throws SQLException {
        this.conexao = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/usuarios",
                "root",
                ""
        );
    }

    public void cadastro(Usuarios u) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, login, senha_hash) VALUES (?, ?, ?)";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, u.GetNome());
        ps.setString(2, u.GetLogin());
        ps.setString(3, u.GetSenha_hash());
        ps.executeUpdate();
        ps.close();
        System.out.println("Cadastro concluído com sucesso!");
    }

    public void remover(String login) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE login = ?";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, login);
        ps.executeUpdate();
        ps.close();
        System.out.println("Usuário removido com sucesso!");
    }
}