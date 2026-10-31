document.addEventListener("DOMContentLoaded", async () => {
    const form = document.getElementById("cadastroForm");
    const botaoSalvar = document.getElementById("botao-cadastrar");
    const selectTutor = document.getElementById("tutor");
    const id = parametro("id");

    if (id) {
        document.getElementById("titulo-form").textContent = "Editar Pet";
        botaoSalvar.textContent = "Salvar";
    }

    try {
        const tutores = await api("/api/tutores");
        selectTutor.replaceChildren(
            new Option(tutores.length ? "Selecione o tutor" : "Nenhum tutor cadastrado", ""),
            ...tutores.map(t => new Option(t.nome + " — CPF " + mascararCpf(t.cpf), t.id))
        );
        if (!tutores.length) {
            mostrarMensagem("Cadastre um tutor antes de cadastrar um pet.", "erro");
        }

        if (id) {
            const pet = await api("/api/pets/" + encodeURIComponent(id));
            form.nome.value = pet.nome;
            form.especie.value = pet.especie;
            form.raca.value = pet.raca;
            form.idade.value = pet.idade;
            selectTutor.value = pet.tutorId;
        }
    } catch (erro) {
        mostrarMensagem(erro.message, "erro");
        botaoSalvar.disabled = true;
        return;
    }

    form.addEventListener("submit", async evento => {
        evento.preventDefault();
        await enviarFormulario({
            botao: botaoSalvar,
            url: id ? "/api/pets/" + encodeURIComponent(id) : "/api/pets",
            metodo: id ? "PUT" : "POST",
            corpo: {
                nome: form.nome.value.trim(),
                especie: form.especie.value.trim(),
                raca: form.raca.value.trim(),
                idade: form.idade.value.trim(),
                tutorId: selectTutor.value ? Number(selectTutor.value) : null
            },
            mapa: { tutorId: "tutor" },
            sucesso: id ? "Pet atualizado com sucesso!" : "Pet cadastrado com sucesso!",
            destino: "pets.html"
        });
    });
});
