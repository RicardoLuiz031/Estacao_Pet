document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    const botaoLogin = document.getElementById("botao-login");

    // Veio redirecionado porque a sessão expirou por inatividade?
    const params = new URLSearchParams(window.location.search);
    if (params.get("expirou") === "1") {
        mostrarMensagem("Sua sessão expirou por inatividade. Faça login novamente.", "erro");
        window.history.replaceState(null, "", window.location.pathname);
    }
    sessionStorage.removeItem("usuario");

    form.addEventListener("submit", async evento => {
        evento.preventDefault();
        limparErros();
        botaoLogin.disabled = true;
        try {
            const dados = await api("/api/login", {
                metodo: "POST",
                corpo: { login: form.email.value.trim(), senha: form.senha.value },
                semRedirecionar: true
            });
            sessionStorage.setItem("usuario", JSON.stringify({ nome: dados.nome, perfil: dados.perfil }));
            window.location.href = "./home.html";
        } catch (erro) {
            mostrarErros(erro, { login: "email", senha: "senha" });
            form.senha.value = "";
            botaoLogin.disabled = false;
        }
    });
});
