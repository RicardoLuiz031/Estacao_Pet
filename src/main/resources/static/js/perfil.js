document.addEventListener("DOMContentLoaded", async () => {
    const form = document.getElementById("senhaForm");
    const botaoSalvar = document.getElementById("botao-salvar");

    document.getElementById("botao-sair").addEventListener("click", () => sair());

    try {
        const eu = await api("/api/me");
        document.getElementById("perfil-usuario").textContent = eu.login;
        document.getElementById("perfil-tipo").textContent = ROTULO_PERFIL[eu.perfil] || eu.perfil;
    } catch (erro) {
        mostrarMensagem(erro.message, "erro");
    }

    form.addEventListener("submit", async evento => {
        evento.preventDefault();
        limparErros();

        const nova = document.getElementById("senha-nova").value;
        if (nova !== document.getElementById("senha-confirmar").value) {
            mostrarErros({ message: "Verifique os campos informados.", campos: { confirmar: "As senhas não conferem." } },
                { confirmar: "senha-confirmar" });
            return;
        }

        const ok = await enviarFormulario({
            botao: botaoSalvar,
            url: "/api/me/senha",
            metodo: "PUT",
            corpo: { senhaAtual: document.getElementById("senha-atual").value, senhaNova: nova },
            mapa: { senhaAtual: "senha-atual", senhaNova: "senha-nova" },
            sucesso: "Senha alterada com sucesso!"
        });
        if (ok) form.reset();
    });
});
