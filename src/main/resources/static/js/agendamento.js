document.addEventListener("DOMContentLoaded", async () => {
    const form = document.getElementById("agendamentoForm");
    const selectPet = document.getElementById("pet");
    const inputData = document.getElementById("data");
    const selectHorario = document.getElementById("horario");
    const botaoAgendar = document.getElementById("botao-agendar");
    const id = parametro("id");

    let statusAtual = null;      // preserva o status ao editar
    let dataOriginal = null;     // data/horário que o agendamento já tinha (edição)
    let horarioOriginal = null;
    let sequencia = 0;

    /** Preenche o select com os horários livres do dia (mantém o horário atual na edição). */
    async function carregarHorarios(selecionado) {
        const minha = ++sequencia;
        selectHorario.replaceChildren(new Option("Selecione o horário", ""));
        if (!inputData.value) return;
        try {
            const url = "/api/agendamentos/horarios?data=" + encodeURIComponent(inputData.value)
                + (id ? "&ignorarId=" + encodeURIComponent(id) : "");
            const livres = await api(url);
            if (minha !== sequencia) return;

            const horarios = new Set(livres);
            if (id && inputData.value === dataOriginal && horarioOriginal) horarios.add(horarioOriginal);
            if (!horarios.size) {
                selectHorario.options[0].text = "Sem horários livres nesta data";
                return;
            }
            [...horarios].sort().forEach(h => selectHorario.add(new Option(h, h)));
            if (selecionado) selectHorario.value = selecionado;
        } catch (erro) {
            mostrarMensagem(erro.message, "erro");
        }
    }

    try {
        const pets = await api("/api/pets");
        selectPet.replaceChildren(
            new Option(pets.length ? "Selecione o pet" : "Nenhum pet cadastrado", ""),
            ...pets.map(p => new Option(p.nome + " (tutor: " + p.tutorNome + ")", p.id))
        );
        if (!pets.length) mostrarMensagem("Cadastre um pet antes de agendar.", "erro");

        if (id) {
            form.querySelector("h3").textContent = "Editar Agendamento";
            botaoAgendar.textContent = "Salvar";
            const a = await api("/api/agendamentos/" + encodeURIComponent(id));
            statusAtual = a.status;
            dataOriginal = a.data;
            horarioOriginal = a.horario;
            selectPet.value = a.petId;
            const radio = form.querySelector('input[name="servico"][value="' + a.servico + '"]');
            if (radio) radio.checked = true;
            inputData.value = a.data;
            await carregarHorarios(a.horario);
        } else {
            inputData.min = hojeISO(); // não permite escolher dias que já passaram
        }
    } catch (erro) {
        mostrarMensagem(erro.message, "erro");
        botaoAgendar.disabled = true;
        return;
    }

    inputData.addEventListener("change", () => carregarHorarios());

    form.addEventListener("submit", async evento => {
        evento.preventDefault();
        const servico = form.querySelector('input[name="servico"]:checked');
        const corpo = {
            petId: selectPet.value ? Number(selectPet.value) : null,
            servico: servico ? servico.value : null,
            data: inputData.value,
            horario: selectHorario.value
        };
        if (id && statusAtual) corpo.status = statusAtual;

        const ok = await enviarFormulario({
            botao: botaoAgendar,
            url: id ? "/api/agendamentos/" + encodeURIComponent(id) : "/api/agendamentos",
            metodo: id ? "PUT" : "POST",
            corpo,
            mapa: { petId: "pet", servico: "grupo-servico" },
            sucesso: id ? "Agendamento atualizado com sucesso!" : "Agendamento realizado com sucesso!",
            destino: "agendamentos.html"
        });
        // Se o horário foi ocupado por outra pessoa enquanto o formulário estava aberto, atualiza a lista
        if (!ok && inputData.value) {
            const mensagem = document.getElementById("mensagem").textContent;
            await carregarHorarios();
            document.getElementById("mensagem").textContent = mensagem;
        }
    });
});
