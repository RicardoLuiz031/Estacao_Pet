document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("funcionarioForm");
    const botaoSalvar = document.getElementById("botao-cadastrar");

    form.addEventListener("submit", async evento => {
        evento.preventDefault();
        await enviarFormulario({
            botao: botaoSalvar,
            url: "/api/funcionarios",
            metodo: "POST",
            corpo: {
                nome: form.nome.value.trim(),
                login: form.email.value.trim(),
                senha: form.senha.value,
                perfil: form.perfil.value
            },
            mapa: { login: "email" },
            sucesso: "Funcionário cadastrado com sucesso!",
            destino: "funcionarios.html"
        });
    });
});
