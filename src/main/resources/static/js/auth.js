// Controle de acesso no navegador. Carregue este arquivo no <head>, ANTES de qualquer outro script.
// Atenção: isto só melhora a experiência; quem protege os dados de verdade é o servidor (SessaoConfig).

function getUsuario() {
    try {
        return JSON.parse(sessionStorage.getItem("usuario"));
    } catch {
        return null;
    }
}

// Limpa os dados locais e vai para o login (expirou = true mostra o aviso de sessão expirada)
function irParaLogin(expirou) {
    sessionStorage.removeItem("usuario");
    window.location.href = "./login.html" + (expirou ? "?expirou=1" : "");
}

function exigirLogin() {
    if (!getUsuario()) {
        irParaLogin(false);
    }
}

function exigirAdmin() {
    const usuario = getUsuario();
    if (!usuario) {
        irParaLogin(false);
    } else if (usuario.perfil !== "ADMIN") {
        window.location.href = "./home.html";
    }
}

// Encerra a sessão também no servidor (invalida o cookie) e volta ao login
function avisarServidorDoLogout() {
    return fetch("/api/logout", { method: "POST", credentials: "same-origin", keepalive: true })
        .catch(() => { /* sem rede: o servidor expira a sessão sozinho */ });
}

function sair() {
    avisarServidorDoLogout().finally(() => irParaLogin(false));
}
