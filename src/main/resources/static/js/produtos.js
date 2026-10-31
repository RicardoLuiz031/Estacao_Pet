document.addEventListener("DOMContentLoaded", () => iniciarLista({
    endpoint: "/api/produtos",
    colunas: 4,
    vazio: "Nenhum produto encontrado.",
    celulas: p => [p.nome, formatarMoeda(p.preco), p.estoque],
    editarHref: p => "cadastro-produto.html?id=" + p.id,
    confirmar: p => 'Excluir o produto "' + p.nome + '"?',
    excluido: "Produto excluído."
}));
