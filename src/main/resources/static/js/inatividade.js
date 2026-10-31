(function () {
    // Só roda em páginas internas (com usuário logado)
    if (typeof getUsuario !== "function" || !getUsuario()) return;

    // Tempos em milissegundos (regra da atividade: 30 s sem nenhuma ação).
    // O servidor aplica a mesma regra em TODA requisição (app.sessao.limite-segundos no application.properties).
    const TEMPO_LIMITE = 30 * 1000;      // inatividade até encerrar a sessão
    const TEMPO_AVISO = 5 * 1000;        // o aviso aparece aos 25 s (item bônus)
    const INTERVALO_PING = 10 * 1000;    // enquanto o usuário está ativo, avisa o servidor a cada 10 s no máximo

    const URL_PING = "/api/sessao/ping";

    let ultimaAtividade = Date.now();
    let ultimoPing = Date.now();
    let atividadePendente = false; // houve atividade desde o último ping?
    let avisoAberto = false;
    let encerrando = false;

    // ---------- Estilo e modal ----------
    const estilo = document.createElement("style");
    estilo.textContent = `
        .inatividade-fundo {
            position: fixed;
            inset: 0;
            background: rgba(0, 0, 0, 0.85);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: 9999;
        }
        .inatividade-fundo.aberto { display: flex; }
        .inatividade-caixa {
            background: #333;
            color: white;
            border: 1px solid #ccc;
            border-radius: 20px;
            padding: 25px;
            width: 90%;
            max-width: 380px;
            text-align: center;
            font-family: Arial, sans-serif;
        }
        .inatividade-caixa h3 { margin: 0 0 10px 0; }
        .inatividade-caixa p { margin: 0 0 20px 0; }
        .inatividade-contagem { color: #4CAF50; font-weight: bold; font-size: 1.2em; }
        .inatividade-botoes { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
        .inatividade-botoes button {
            border-radius: 30px;
            padding: 10px 20px;
            font-weight: bold;
            cursor: pointer;
            color: white;
        }
        #inatividade-continuar { background: #4CAF50; border: none; }
        #inatividade-continuar:hover { background: #45a049; }
        #inatividade-sair { background: transparent; border: 1px solid #e53935; }
        #inatividade-sair:hover { background: #e53935; }
    `;
    document.head.appendChild(estilo);

    const fundo = document.createElement("div");
    fundo.className = "inatividade-fundo";
    fundo.setAttribute("role", "alertdialog");
    fundo.setAttribute("aria-modal", "true");
    fundo.innerHTML = `
        <div class="inatividade-caixa">
            <h3 id="inatividade-titulo">Sessão prestes a expirar</h3>
            <p id="inatividade-texto">Por inatividade, sua sessão será encerrada em
               <span class="inatividade-contagem" id="inatividade-segundos">5</span> segundos.</p>
            <div class="inatividade-botoes" id="inatividade-botoes">
                <button type="button" id="inatividade-continuar">Continuar conectado</button>
                <button type="button" id="inatividade-sair">Sair agora</button>
            </div>
        </div>`;
    document.body.appendChild(fundo);

    const spanSegundos = fundo.querySelector("#inatividade-segundos");
    const botaoContinuar = fundo.querySelector("#inatividade-continuar");

    // ---------- Ações ----------
    // Bloqueia a tela, avisa o servidor e volta ao login com a mensagem de expiração
    function encerrar() {
        if (encerrando) return;
        encerrando = true;
        fundo.querySelector("#inatividade-titulo").textContent = "Sessão expirada";
        fundo.querySelector("#inatividade-texto").textContent =
            "Sua sessão foi encerrada por inatividade. Redirecionando para o login...";
        fundo.querySelector("#inatividade-botoes").style.display = "none";
        fundo.classList.add("aberto");
        avisarServidorDoLogout();
        setTimeout(() => irParaLogin(true), 1200);
    }

    // Renova a sessão no servidor. 401 significa que ela já expirou lá.
    async function renovarNoServidor() {
        ultimoPing = Date.now();
        atividadePendente = false;
        try {
            const resposta = await fetch(URL_PING, { method: "GET", credentials: "same-origin" });
            if (resposta.status === 401) {
                encerrar();
            }
        } catch (erro) {
            // Sem rede: ignora; o servidor continua validando a expiração em cada requisição
        }
    }

    function fecharAviso() {
        avisoAberto = false;
        fundo.classList.remove("aberto");
    }

    function continuarConectado() {
        ultimaAtividade = Date.now();
        fecharAviso();
        renovarNoServidor();
    }

    function registrarAtividade() {
        if (avisoAberto || encerrando) return; // com o aviso aberto, só os botões valem
        ultimaAtividade = Date.now();
        atividadePendente = true;
    }

    botaoContinuar.addEventListener("click", continuarConectado);
    fundo.querySelector("#inatividade-sair").addEventListener("click", () => sair());

    // Cada um destes eventos reinicia a contagem (uma única contagem, sem criar timers novos)
    ["mousemove", "mousedown", "keydown", "scroll", "wheel", "touchstart", "click"].forEach(tipo => {
        document.addEventListener(tipo, registrarAtividade, { passive: true, capture: true });
    });

    // ---------- Contagem ----------
    // Usa o horário real (Date.now) e não um contador, porque abas em segundo plano
    // têm os timers atrasados pelo navegador. Ao voltar para a aba, verifica na hora.
    function verificar() {
        if (encerrando) return;
        const agora = Date.now();
        const ocioso = agora - ultimaAtividade;

        if (ocioso >= TEMPO_LIMITE) {
            encerrar();
        } else if (ocioso >= TEMPO_LIMITE - TEMPO_AVISO) {
            if (!avisoAberto) {
                avisoAberto = true;
                fundo.classList.add("aberto");
                botaoContinuar.focus();
            }
            spanSegundos.textContent = Math.ceil((TEMPO_LIMITE - ocioso) / 1000);
        } else if (atividadePendente && agora - ultimoPing >= INTERVALO_PING) {
            // Usuário ativo: mantém a sessão do servidor viva mesmo sem chamadas à API
            renovarNoServidor();
        }
    }

    setInterval(verificar, 1000);
    document.addEventListener("visibilitychange", verificar);
})();
