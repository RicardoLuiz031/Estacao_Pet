/* =====================================================================
 * comum.js — funções usadas por todas as telas internas.
 * Carregue DEPOIS do auth.js e ANTES dos scripts de cada página.
 * ===================================================================== */
const API_BASE = "http://127.0.0.1:8080";
// ---------- Rótulos para exibir os valores que a API devolve ----------
const ROTULO_SERVICO = { BANHO: "Banho", TOSA: "Tosa", BANHO_E_TOSA: "Banho e Tosa" };
const ROTULO_STATUS = {
    AGENDADO: "Agendado",
    EM_ANDAMENTO: "Em andamento",
    CONCLUIDO: "Concluído",
    CANCELADO: "Cancelado"
};
const ROTULO_PERFIL = { ADMIN: "Administrador", ATENDENTE: "Atendente" };

// ---------- Comunicação com a API ----------
class ErroApi extends Error {
    constructor(status, dados) {
        super((dados && dados.erro) || "Não foi possível concluir a operação.");
        this.status = status;
        this.codigo = dados && dados.codigo;
        this.campos = (dados && dados.campos) || {};
    }
}

/**
 * Chama a API (JSON) e devolve o corpo da resposta (ou null em 204).
 * - Em caso de erro, lança ErroApi com { message, campos, status }.
 * - Se o servidor responder 401 (sem login / sessão expirada), volta para o login,
 *   a não ser que semRedirecionar seja true (usado na própria tela de login).
 */
async function api(url, { metodo = "GET", corpo, semRedirecionar = false } = {}) {
    const opcoes = { method: metodo, credentials: "include", headers: {} };
    if (corpo !== undefined) {
        opcoes.headers["Content-Type"] = "application/json";
        opcoes.body = JSON.stringify(corpo);
    }

    let resposta;
    try {
        resposta = await fetch(API_BASE + url, opcoes);
    } catch (e) {
        throw new ErroApi(0, { erro: "Não foi possível conectar ao servidor." });
    }

    let dados = null;
    if (resposta.status !== 204) {
        const texto = await resposta.text();
        if (texto) {
            try {
                dados = JSON.parse(texto);
            } catch (e) {
                dados = null;
            }
        }
    }

    if (resposta.ok) return dados;

    if (resposta.status === 401 && !semRedirecionar) {
        irParaLogin(dados && dados.codigo === "SESSAO_EXPIRADA");
    }
    throw new ErroApi(resposta.status, dados);
}

// ---------- Mensagens e erros de formulário ----------
function mostrarMensagem(texto, tipo) {
    const el = document.getElementById("mensagem");
    if (!el) return;
    el.textContent = texto || "";
    el.className = tipo || "";
}

function limparErros() {
    document.querySelectorAll(".erro-campo").forEach(e => e.remove());
    document.querySelectorAll(".invalido").forEach(e => e.classList.remove("invalido"));
    mostrarMensagem("");
}

/**
 * Mostra o erro geral em #mensagem e cada erro de campo logo abaixo do campo.
 * "mapa" traduz o nome do campo da API para o id do elemento (ex.: { tutorId: "tutor" }).
 */
function mostrarErros(erro, mapa = {}) {
    const semCampo = [];
    let primeiro = null;

    for (const [campo, texto] of Object.entries(erro.campos || {})) {
        const alvo = document.getElementById(mapa[campo] || campo);
        if (!alvo) {
            semCampo.push(texto);
            continue;
        }
        alvo.classList.add("invalido");
        const aviso = document.createElement("small");
        aviso.className = "erro-campo";
        aviso.textContent = texto;
        alvo.insertAdjacentElement("afterend", aviso);
        if (!primeiro) primeiro = alvo;
    }

    mostrarMensagem([erro.message, ...semCampo].join(" "), "erro");
    if (primeiro && typeof primeiro.focus === "function") primeiro.focus();
}

/** Envia o formulário (POST/PUT), mostra sucesso/erros e, se houver, redireciona. Devolve true se deu certo. */
async function enviarFormulario({ botao, url, metodo, corpo, mapa, sucesso, destino }) {
    limparErros();
    botao.disabled = true;
    try {
        await api(url, { metodo, corpo });
        mostrarMensagem(sucesso, "sucesso");
        if (destino) {
            setTimeout(() => { window.location.href = destino; }, 1000);
        } else {
            botao.disabled = false;
        }
        return true;
    } catch (erro) {
        mostrarErros(erro, mapa);
        botao.disabled = false;
        return false;
    }
}

// ---------- Formatação e máscaras ----------
function somenteDigitos(texto) {
    return (texto || "").replace(/\D/g, "");
}

function formatarCpf(valor) {
    const d = somenteDigitos(valor).slice(0, 11);
    let r = d.slice(0, 3);
    if (d.length > 3) r += "." + d.slice(3, 6);
    if (d.length > 6) r += "." + d.slice(6, 9);
    if (d.length > 9) r += "-" + d.slice(9, 11);
    return r;
}

/** Na listagem o CPF aparece parcialmente oculto (princípio da necessidade, LGPD). */
function mascararCpf(valor) {
    const d = somenteDigitos(valor);
    if (d.length !== 11) return valor || "";
    return "***." + d.slice(3, 6) + "." + d.slice(6, 9) + "-**";
}

function formatarTelefone(valor) {
    const d = somenteDigitos(valor).slice(0, 11);
    if (!d) return "";
    let r = "(" + d.slice(0, 2);
    if (d.length >= 3) {
        r += ") ";
        if (d.length > 10) {
            r += d.slice(2, 7) + "-" + d.slice(7);
        } else {
            r += d.slice(2, 6) + (d.length > 6 ? "-" + d.slice(6) : "");
        }
    }
    return r;
}

function formatarCep(valor) {
    const d = somenteDigitos(valor).slice(0, 8);
    return d.length > 5 ? d.slice(0, 5) + "-" + d.slice(5) : d;
}

function aplicarMascara(input, formatador) {
    if (!input) return;
    input.addEventListener("input", () => { input.value = formatador(input.value); });
}

function formatarData(iso) {
    if (!iso) return "";
    const [ano, mes, dia] = iso.split("-");
    return dia + "/" + mes + "/" + ano;
}

function formatarMoeda(numero) {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(numero);
}

function hojeISO() {
    const d = new Date();
    const dois = n => String(n).padStart(2, "0");
    return d.getFullYear() + "-" + dois(d.getMonth() + 1) + "-" + dois(d.getDate());
}

// ---------- Pequenos utilitários de DOM (usam textContent: sem risco de XSS) ----------
function parametro(nome) {
    return new URLSearchParams(window.location.search).get(nome);
}

function debounce(funcao, ms = 300) {
    let temporizador;
    return (...args) => {
        clearTimeout(temporizador);
        temporizador = setTimeout(() => funcao(...args), ms);
    };
}

function celula(texto, classe) {
    const td = document.createElement("td");
    td.textContent = texto === undefined || texto === null ? "" : texto;
    if (classe) td.className = classe;
    return td;
}

function botao(texto, classe, aoClicar) {
    const b = document.createElement("button");
    b.type = "button";
    b.className = classe;
    b.textContent = texto;
    b.addEventListener("click", aoClicar);
    return b;
}

function linkBotao(texto, classe, href) {
    const a = document.createElement("a");
    a.className = classe;
    a.textContent = texto;
    a.href = href;
    return a;
}

function linhaVazia(corpo, colunas, texto) {
    const tr = document.createElement("tr");
    const td = celula(texto, "vazio");
    td.colSpan = colunas;
    tr.append(td);
    corpo.replaceChildren(tr);
}

/**
 * Listagem padrão (tutores, pets, produtos, funcionários): busca por nome,
 * tabela, botão Editar (opcional) e Excluir com confirmação.
 * cfg = { endpoint, colunas, vazio, celulas(item) -> [textos], editarHref(item),
 *         confirmar(item) -> texto, excluido -> texto }
 */
function iniciarLista(cfg) {
    const corpo = document.getElementById("tabela-corpo");
    const busca = document.getElementById("busca");
    let sequencia = 0; // descarta respostas antigas se o usuário digitar rápido

    async function carregar(manterMensagem) {
        const minha = ++sequencia;
        const termo = busca ? busca.value.trim() : "";
        try {
            const itens = await api(cfg.endpoint + (termo ? "?busca=" + encodeURIComponent(termo) : ""));
            if (minha !== sequencia) return;
            desenhar(itens);
            if (!manterMensagem) mostrarMensagem("");
        } catch (erro) {
            if (minha !== sequencia) return;
            linhaVazia(corpo, cfg.colunas, "Não foi possível carregar a lista.");
            mostrarMensagem(erro.message, "erro");
        }
    }

    function desenhar(itens) {
        if (!itens.length) {
            linhaVazia(corpo, cfg.colunas, cfg.vazio);
            return;
        }
        corpo.replaceChildren(...itens.map(item => {
            const tr = document.createElement("tr");
            cfg.celulas(item).forEach(texto => tr.append(celula(texto)));
            const acoes = celula("", "col-acoes");
            if (cfg.editarHref) acoes.append(linkBotao("Editar", "btn-editar", cfg.editarHref(item)));
            acoes.append(botao("Excluir", "btn-excluir", () => excluir(item)));
            tr.append(acoes);
            return tr;
        }));
    }

    async function excluir(item) {
        if (!window.confirm(cfg.confirmar(item))) return;
        try {
            await api(cfg.endpoint + "/" + item.id, { metodo: "DELETE" });
            await carregar(true);
            mostrarMensagem(cfg.excluido, "sucesso");
        } catch (erro) {
            mostrarMensagem(erro.message, "erro");
        }
    }

    if (busca) busca.addEventListener("input", debounce(() => carregar(), 300));
    carregar();
}
