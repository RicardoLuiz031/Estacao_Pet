package repository;

import model.Agendamentos;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AgendamentosDAO {

    private Connection conexao;

    public AgendamentosDAO() throws SQLException {
        this.conexao = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/usuarios",
                "root",
                ""
        );
    }

    // Verificar se já existe agendamento na mesma data/horário
    public boolean existeAgendamentoNoHorario(String dataHora) throws SQLException {
        String sql = "SELECT COUNT(*) FROM agendamentos WHERE data_hora = ? AND status != 'cancelado'";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, dataHora);
        ResultSet rs = ps.executeQuery();

        boolean existe = false;
        if (rs.next()) {
            existe = rs.getInt(1) > 0;
        }

        rs.close();
        ps.close();
        return existe;
    }

    // Cadastrar agendamento (com validação de choque de horário)
    public boolean cadastro(Agendamentos a) throws SQLException {
        if (existeAgendamentoNoHorario(a.GetData_hora())) {
            System.out.println("Erro: Já existe um agendamento marcado para este horário!");
            return false;
        }

        String sql = "INSERT INTO agendamentos (servicos, data_hora, status) VALUES (?, ?, ?)";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, a.GetServicos());
        ps.setString(2, a.GetData_hora());
        ps.setString(3, a.GetStatus());
        ps.executeUpdate();
        ps.close();
        System.out.println("Agendamento cadastrado com sucesso!");
        return true;
    }

    // Listar todos os agendamentos
    public List<Agendamentos> listar() throws SQLException {
        List<Agendamentos> lista = new ArrayList<>();
        String sql = "SELECT * FROM agendamentos";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String servicos = rs.getString("servicos");
            String dataHora = rs.getString("data_hora");
            String status = rs.getString("status");

            Agendamentos a = new Agendamentos(servicos, dataHora, status);
            lista.add(a);
        }

        rs.close();
        ps.close();
        return lista;
    }

    // Atualizar status do agendamento (ex: agendado, em andamento, concluído, cancelado)
    public void atualizarStatus(String dataHora, String novoStatus) throws SQLException {
        String sql = "UPDATE agendamentos SET status = ? WHERE data_hora = ?";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, novoStatus);
        ps.setString(2, dataHora);
        ps.executeUpdate();
        ps.close();
        System.out.println("Status do agendamento atualizado com sucesso!");
    }

    // Cancelar/Remover agendamento
    public void remover(String dataHora) throws SQLException {
        String sql = "DELETE FROM agendamentos WHERE data_hora = ?";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, dataHora);
        ps.executeUpdate();
        ps.close();
        System.out.println("Agendamento removido com sucesso!");
    }
}