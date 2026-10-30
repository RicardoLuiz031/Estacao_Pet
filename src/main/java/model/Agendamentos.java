package model;

public class Agendamentos {
    //Atributos
    private String servicos;
    private String data_hora;
    private String status;


    //construtor
    public Agendamentos(String servicos,String data_hora,String status) {
        this.servicos = servicos;
        this.data_hora = data_hora;
        this.status = status;
    }

    //Getters e Setters
    public String GetServicos() {
        return servicos;
    }
    public void setServicos(String servicos) {
        this.servicos = servicos;
    }

    public String GetData_hora() {
        return data_hora;
    }
    public void setData_hora(String data_hora) {
        this.data_hora = data_hora;
    }

    public String GetStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    //Exibe os dados
    @Override
    public String toString() {
        return "\n === AGENDAMENTOS ===" +
                "\nServicos: " + servicos +
                "\nData_hora: " + data_hora +
                "\nStatus: " + status;
    }
}