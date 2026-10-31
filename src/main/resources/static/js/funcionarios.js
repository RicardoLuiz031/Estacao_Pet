document.addEventListener("DOMContentLoaded", () => iniciarLista({
    endpoint: "/api/funcionarios",
    colunas: 4,
    vazio: "Nenhum funcionário encontrado.",
    celulas: f => [f.nome, f.login, ROTULO_PERFIL[f.perfil] || f.perfil],
    confirmar: f => 'Excluir o funcionário "' + f.login + '"?',
    excluido: "Funcionário excluído."
}));
