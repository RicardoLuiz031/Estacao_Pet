package repository;

import model.Tutor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TutorDAO {

    private Connection conexao;

    public TutorDAO() throws SQLException {
        this.conexao = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/usuarios",
                "root",
                ""
        );
    }

    // Cadastrar tutor (com campos criptografados / cifrados)
    public void cadastro(Tutor t) throws SQLException {
        String sql = "INSERT INTO tutor (nome, cpf_cif, telefone_cif, email_cif, endereco_cif) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, t.GetNome());
        ps.setString(2, t.GetCpf_cif());
        ps.setString(3, t.GetEmail_cif()); // atenção aos getters do seu modelo
        ps.setString(4, t.GetEmail_cif());
        ps.setString(5, t.GetEndereco_cif());
        ps.executeUpdate();
        ps.close();
        System.out.println("Tutor cadastrado com sucesso!");
    }

    // Listar todos os tutores
    public List<Tutor> listar() throws SQLException {
        List<Tutor> lista = new ArrayList<>();
        String sql = "SELECT * FROM tutor";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String nome = rs.getString("nome");
            String cpfCif = rs.getString("cpf_cif");
            String telefoneCif = rs.getString("telefone_cif");
            String emailCif = rs.getString("email_cif");
            String enderecoCif = rs.getString("endereco_cif");

            Tutor t = new Tutor(nome, cpfCif, telefoneCif, emailCif, enderecoCif);
            lista.add(t);
        }

        rs.close();
        ps.close();
        return lista;
    }

    // Atualizar dados do tutor pelo nome
    public void atualizar(Tutor t) throws SQLException {
        String sql = "UPDATE tutor SET cpf_cif = ?, telefone_cif = ?, email_cif = ?, endereco_cif = ? WHERE nome = ?";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, t.GetCpf_cif());
        ps.setString(2, t.GetEmail_cif());
        ps.setString(3, t.GetEmail_cif());
        ps.setString(4, t.GetEndereco_cif());
        ps.setString(5, t.GetNome());
        ps.executeUpdate();
        ps.close();
        System.out.println("Dados do tutor atualizados com sucesso!");
    }

    // Remover tutor pelo nome
    public void remover(String nome) throws SQLException {
        String sql = "DELETE FROM tutor WHERE nome = ?";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, nome);
        ps.executeUpdate();
        ps.close();
        System.out.println("Tutor removido com sucesso!");
    }
}