document.addEventListener("DOMContentLoaded", async () => {
    const corpo = document.getElementById("tabela-corpo");

    document.getElementById("data-hoje").textContent = new Date().toLocaleDateString("pt-BR", {
        weekday: "long", day: "numeric", month: "long", year: "numeric"
    });

    try {
        const painel = await api("/api/painel");
        document.getElementById("saudacao").textContent = "Olá, " + (painel.nome || "bem-vindo") + "!";
        document.getElementById("total-agendamentos").textContent = painel.totalAgendamentosHoje;
        document.getElementById("total-clientes").textContent = painel.totalClientes;

        const lista = painel.agendamentosHoje || [];
        if (!lista.length) {
            linhaVazia(corpo, 4, "Nenhum agendamento para hoje.");
            return;
        }
        corpo.replaceChildren(...lista.map(a => {
            const tr = document.createElement("tr");
            const selo = document.createElement("span");
            selo.className = "status status-" + a.status.toLowerCase();
            selo.textContent = ROTULO_STATUS[a.status] || a.status;
            const tdStatus = celula("");
            tdStatus.append(selo);
            tr.append(
                celula(a.horario),
                celula(a.petNome + " (" + a.tutorNome + ")"),
                celula(ROTULO_SERVICO[a.servico] || a.servico),
                tdStatus
            );
            return tr;
        }));
    } catch (erro) {
        linhaVazia(corpo, 4, "Não foi possível carregar o painel.");
        mostrarMensagem(erro.message, "erro");
    }
});
