package estacaoPet.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
public class Agendamentos {
    //Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agendamentos")
    private Integer id;
    @Convert(converter = ServicoConverter.class)
    @Column(name = "servicosAgendamento", nullable = false)
    private Servico servico;
    // UNIQUE no banco: impede dois agendamentos no mesmo horário
    @Column(name = "dataHoraAgendamento", nullable = false, unique = true)
    private LocalDateTime dataHora;
    @Convert(converter = StatusConverter.class)
    @Column(name = "statusAgendamento", nullable = false)
    private StatusAgendamento status;

    //Relacionando tabelas
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_animais", nullable = false)
    private Animais animais;

    // Valores iguais aos do ENUM do banco: 'banho', 'tosa', 'ambos'
    // (os nomes em maiúsculo são os que o frontend envia)
    public enum Servico {
        BANHO("banho"),
        TOSA("tosa"),
        BANHO_E_TOSA("ambos");

        private final String valorBanco;

        Servico(String valorBanco) {
            this.valorBanco = valorBanco;
        }

        public String getValorBanco() {
            return valorBanco;
        }
    }

    // Valores iguais aos do ENUM do banco: 'agendado', 'concluido', 'cancelado', 'em andamento'
    public enum StatusAgendamento {
        AGENDADO("agendado"),
        CONCLUIDO("concluido"),
        CANCELADO("cancelado"),
        EM_ANDAMENTO("em andamento");

        private final String valorBanco;

        StatusAgendamento(String valorBanco) {
            this.valorBanco = valorBanco;
        }

        public String getValorBanco() {
            return valorBanco;
        }
    }

    @Converter
    public static class ServicoConverter implements AttributeConverter<Servico, String> {
        @Override
        public String convertToDatabaseColumn(Servico servico) {
            return servico == null ? null : servico.getValorBanco();
        }

        @Override
        public Servico convertToEntityAttribute(String valor) {
            if (valor == null) {
                return null;
            }
            for (Servico s : Servico.values()) {
                if (s.getValorBanco().equals(valor)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Serviço desconhecido no banco: " + valor);
        }
    }

    // Converte o enum do Java para o texto que o banco guarda (e vice-versa)
    @Converter
    public static class StatusConverter implements AttributeConverter<StatusAgendamento, String> {
        @Override
        public String convertToDatabaseColumn(StatusAgendamento status) {
            return status == null ? null : status.getValorBanco();
        }

        @Override
        public StatusAgendamento convertToEntityAttribute(String valor) {
            if (valor == null) {
                return null;
            }
            for (StatusAgendamento s : StatusAgendamento.values()) {
                if (s.getValorBanco().equals(valor)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Status desconhecido no banco: " + valor);
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public Animais getAnimais() {
        return animais;
    }

    public void setAnimais(Animais animais) {
        this.animais = animais;
    }
}
