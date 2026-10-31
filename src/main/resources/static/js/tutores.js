document.addEventListener("DOMContentLoaded", () => iniciarLista({
    endpoint: "/api/tutores",
    colunas: 6,
    vazio: "Nenhum tutor encontrado.",
    celulas: t => [t.nome, formatarTelefone(t.telefone), t.email || "-", mascararCpf(t.cpf), t.endereco || "-"],
    editarHref: t => "cadastro-tutor.html?id=" + t.id,
    confirmar: t => 'Excluir o tutor "' + t.nome + '"?',
    excluido: "Tutor excluído."
}));
