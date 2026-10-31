document.addEventListener("DOMContentLoaded", async () => {
    const container = document.getElementById("header-container");
    if (!container) return;

    try {
        const resposta = await fetch("./header.html");
        if (!resposta.ok) throw new Error("Status " + resposta.status);

        container.innerHTML = await resposta.text();

        // Itens exclusivos do administrador (ex.: Funcionários) somem para os demais perfis
        const usuario = typeof getUsuario === "function" ? getUsuario() : null;
        if (!usuario || usuario.perfil !== "ADMIN") {
            container.querySelectorAll("[data-somente-admin]").forEach(item => item.remove());
        }

        // Marca como ativo o link da página atual (as telas de cadastro destacam a listagem)
        const mesmaSecao = {
            "cadastro-tutor.html": "tutores.html",
            "cadastro-pet.html": "pets.html",
            "cadastro-agendamento.html": "agendamentos.html",
            "cadastro-produto.html": "produtos.html",
            "cadastro-funcionario.html": "funcionarios.html"
        };
        const arquivo = location.pathname.split("/").pop() || "home.html";
        const paginaAtual = mesmaSecao[arquivo] || arquivo;
        container.querySelectorAll(".nav-links .nav-item").forEach(link => {
            if (link.getAttribute("href").replace("./", "") === paginaAtual) {
                link.classList.add("ativo");
            }
        });

        // "Sair" encerra a sessão no servidor também
        const botaoSair = container.querySelector("#botao-sair-header");
        if (botaoSair && typeof sair === "function") {
            botaoSair.addEventListener("click", evento => {
                evento.preventDefault();
                sair();
            });
        }
    } catch (erro) {
        console.error("Erro ao carregar o header:", erro);
    }
});
