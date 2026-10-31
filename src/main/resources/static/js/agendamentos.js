document.addEventListener("DOMContentLoaded", () => {
    const corpo = document.getElementById("tabela-corpo");
    const filtroData = document.getElementById("filtro-data");
    const filtroStatus = document.getElementById("filtro-status");
    let lista = [];
    let sequencia = 0;

    async function carregar(manterMensagem) {
        const minha = ++sequencia;
        const params = new URLSearchParams();
        if (filtroData.value) params.set("data", filtroData.value);
        if (filtroStatus.value) params.set("status", filtroStatus.value);
        try {
            const dados = await api("/api/agendamentos" + (params.toString() ? "?" + params : ""));
            if (minha !== sequencia) return;
            lista = dados;
            desenhar();
            if (!manterMensagem) mostrarMensagem("");
        } catch (erro) {
            if (minha !== sequencia) return;
            lista = [];
            linhaVazia(corpo, 6, "Não foi possível carregar os agendamentos.");
            mostrarMensagem(erro.message, "erro");
        }
    }

    function desenhar() {
        if (!lista.length) {
            linhaVazia(corpo, 6, "Nenhum agendamento encontrado.");
            return;
        }
        corpo.replaceChildren(...lista.map(a => {
            const tr = document.createElement("tr");

            const seletor = document.createElement("select");
            seletor.className = "select-status";
            seletor.setAttribute("aria-label", "Status do agendamento");
            Object.entries(ROTULO_STATUS).forEach(([valor, rotulo]) => seletor.add(new Option(rotulo, valor)));
            seletor.value = a.status;
            seletor.addEventListener("change", () => mudarStatus(a, seletor));
            const tdStatus = celula("");
            tdStatus.append(seletor);

            const acoes = celula("", "col-acoes");
            acoes.append(
                linkBotao("Editar", "btn-editar", "cadastro-agendamento.html?id=" + a.id),
                botao("Excluir", "btn-excluir", () => excluir(a))
            );

            tr.append(
                celula(formatarData(a.data)),
                celula(a.horario),
                celula(a.petNome + " (" + a.tutorNome + ")"),
                celula(ROTULO_SERVICO[a.servico] || a.servico),
                tdStatus,
                acoes
            );
            return tr;
        }));
    }

    async function mudarStatus(agendamento, seletor) {
        try {
            await api("/api/agendamentos/" + agendamento.id + "/status", {
                metodo: "PATCH",
                corpo: { status: seletor.value }
            });
            agendamento.status = seletor.value;
            mostrarMensagem("Status atualizado.", "sucesso");
        } catch (erro) {
            seletor.value = agendamento.status;
            mostrarMensagem(erro.message, "erro");
        }
    }

    async function excluir(a) {
        const quando = formatarData(a.data) + " às " + a.horario;
        if (!window.confirm("Excluir o agendamento de " + a.petNome + " (" + quando + ")?")) return;
        try {
            await api("/api/agendamentos/" + a.id, { metodo: "DELETE" });
            await carregar(true);
            mostrarMensagem("Agendamento excluído.", "sucesso");
        } catch (erro) {
            mostrarMensagem(erro.message, "erro");
        }
    }

    // ---------- Exportação CSV (item bônus): exporta o que está na tela ----------
    function celulaCsv(valor) {
        let texto = String(valor === undefined || valor === null ? "" : valor);
        if (/^[=+\-@\t\r]/.test(texto)) texto = "'" + texto; // evita injeção de fórmula no Excel
        return '"' + texto.replace(/"/g, '""') + '"';
    }

    function exportarCsv() {
        if (!lista.length) {
            mostrarMensagem("Não há agendamentos para exportar.", "erro");
            return;
        }
        const linhas = [["Data", "Horário", "Pet", "Tutor", "Serviço", "Status"]];
        lista.forEach(a => linhas.push([
            formatarData(a.data), a.horario, a.petNome, a.tutorNome,
            ROTULO_SERVICO[a.servico] || a.servico, ROTULO_STATUS[a.status] || a.status
        ]));
        const csv = "\uFEFF" + linhas.map(l => l.map(celulaCsv).join(";")).join("\r\n");
        const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
        const link = document.createElement("a");
        link.href = url;
        link.download = "agendamentos-" + (filtroData.value || "todos") + ".csv";
        document.body.append(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
    }

    filtroData.addEventListener("change", () => carregar());
    filtroStatus.addEventListener("change", () => carregar());
    document.getElementById("botao-limpar").addEventListener("click", () => {
        filtroData.value = "";
        filtroStatus.value = "";
        carregar();
    });
    document.getElementById("botao-exportar").addEventListener("click", exportarCsv);

    carregar();
});
