document.addEventListener("DOMContentLoaded", async () => {
    const form = document.getElementById("cadastroForm");
    const botaoSalvar = document.getElementById("botao-cadastrar");
    const id = parametro("id");

    aplicarMascara(form.cpf, formatarCpf);
    aplicarMascara(form.telefone, formatarTelefone);
    aplicarMascara(form.cep, formatarCep);
    form.estado.addEventListener("input", () => { form.estado.value = form.estado.value.toUpperCase(); });

    // A API guarda o endereço em um único texto:
    // "Rua, Número, Bairro, Cidade/UF, CEP 00000-000[, Observação]"
    const limpar = texto => texto.replace(/,/g, " ").replace(/\s+/g, " ").trim();

    function montarEndereco() {
        let texto = [limpar(form.rua.value), limpar(form.numero.value), limpar(form.bairro.value)].join(", ")
            + ", " + limpar(form.cidade.value) + "/" + form.estado.value.trim().toUpperCase()
            + ", CEP " + formatarCep(form.cep.value);
        const obs = limpar(form.observacao.value);
        if (obs) texto += ", " + obs;
        return texto;
    }

    function preencherEndereco(endereco) {
        const m = /^(.*?), (.*?), (.*?), (.*?)\/([A-Za-z]{2}), CEP (\d{5}-?\d{3})(?:, (.*))?$/.exec(endereco || "");
        if (m) {
            form.rua.value = m[1];
            form.numero.value = m[2];
            form.bairro.value = m[3];
            form.cidade.value = m[4];
            form.estado.value = m[5].toUpperCase();
            form.cep.value = formatarCep(m[6]);
            form.observacao.value = m[7] || "";
        } else if (endereco) {
            form.rua.value = endereco; // formato antigo/desconhecido: o usuário reorganiza
        }
    }

    if (id) {
        document.getElementById("titulo-form").textContent = "Editar Tutor";
        botaoSalvar.textContent = "Salvar";
        try {
            const t = await api("/api/tutores/" + encodeURIComponent(id));
            const partes = t.nome.trim().split(/\s+/);
            form.nome.value = partes.shift();
            form.sobrenome.value = partes.join(" ");
            form.telefone.value = formatarTelefone(t.telefone);
            form.cpf.value = formatarCpf(t.cpf);
            form.email.value = t.email || "";
            preencherEndereco(t.endereco);
        } catch (erro) {
            mostrarMensagem(erro.message, "erro");
            botaoSalvar.disabled = true;
            return;
        }
    }

    form.addEventListener("submit", async evento => {
        evento.preventDefault();
        await enviarFormulario({
            botao: botaoSalvar,
            url: id ? "/api/tutores/" + encodeURIComponent(id) : "/api/tutores",
            metodo: id ? "PUT" : "POST",
            corpo: {
                nome: (form.nome.value.trim() + " " + form.sobrenome.value.trim()).trim(),
                telefone: somenteDigitos(form.telefone.value),
                email: form.email.value.trim(),
                cpf: somenteDigitos(form.cpf.value),
                endereco: montarEndereco()
            },
            mapa: { endereco: "rua" },
            sucesso: id ? "Tutor atualizado com sucesso!" : "Tutor cadastrado com sucesso!",
            destino: "tutores.html"
        });
    });
});
