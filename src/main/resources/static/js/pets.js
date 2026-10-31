document.addEventListener("DOMContentLoaded", () => iniciarLista({
    endpoint: "/api/pets",
    colunas: 6,
    vazio: "Nenhum pet encontrado.",
    celulas: p => [p.nome, p.especie, p.raca, p.idade, p.tutorNome],
    editarHref: p => "cadastro-pet.html?id=" + p.id,
    confirmar: p => 'Excluir o pet "' + p.nome + '"?',
    excluido: "Pet excluído."
}));
