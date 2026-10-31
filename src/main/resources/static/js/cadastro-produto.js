document.addEventListener("DOMContentLoaded", async () => {
    const form = document.getElementById("produtoForm");
    const botaoSalvar = document.getElementById("botao-cadastrar");
    const id = parametro("id");

    if (id) {
        document.getElementById("titulo-form").textContent = "Editar Produto";
        botaoSalvar.textContent = "Salvar";
        try {
            const p = await api("/api/produtos/" + encodeURIComponent(id));
            form.nome.value = p.nome;
            form.preco.value = p.preco;
            form.estoque.value = p.estoque;
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
            url: id ? "/api/produtos/" + encodeURIComponent(id) : "/api/produtos",
            metodo: id ? "PUT" : "POST",
            corpo: {
                nome: form.nome.value.trim(),
                preco: form.preco.value === "" ? null : Number(form.preco.value),
                estoque: form.estoque.value === "" ? null : parseInt(form.estoque.value, 10)
            },
            sucesso: id ? "Produto atualizado com sucesso!" : "Produto cadastrado com sucesso!",
            destino: "produtos.html"
        });
    });
});
