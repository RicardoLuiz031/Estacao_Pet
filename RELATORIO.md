# Relatório — Estação Pet (decisões de segurança e dificuldades)

**Curso:** Técnico em Desenvolvimento de Sistemas — SENAI · **Stack:** Spring Boot, MySQL, HTML/CSS/JS puro

## Decisões de segurança

1. **Senhas:** guardadas com BCrypt (hash com sal) via Spring Security. O login compara o hash mesmo quando o e-mail não existe (hash falso), para o tempo de resposta não revelar quais e-mails estão cadastrados.
2. **Dados sensíveis:** CPF, telefone, e-mail e endereço do tutor são cifrados com AES-GCM antes de ir ao banco (conversor JPA). O banco não tem senha nem CPF legível.
3. **Chave fora do código:** a chave vem da variável de ambiente `ESTACAO_CHAVE`; sem ela a aplicação nem inicia. A chave de AES e a do HMAC são derivadas dela com finalidades separadas.
4. **Busca por CPF:** como o texto cifrado não aceita `LIKE`, o CPF também é gravado como HMAC-SHA256. A busca por CPF é exata (pelo hash); a busca por nome usa o nome em texto claro.
5. **Sessão de 30 s:** o navegador reinicia a contagem a cada clique, tecla, rolagem ou movimento do mouse e avisa aos 25 s. Como o cliente pode ser manipulado, o servidor (`SessaoConfig`) confere a última atividade em **toda** requisição `/api/**` e devolve 401 quando passa de 30 s (mais 5 s de folga para o ping). A tela é bloqueada e o usuário volta ao login com mensagem explicativa.
6. **Perfis:** `ADMIN` e `ATENDENTE`; `/api/funcionarios` responde 403 para quem não é administrador, mesmo que alguém abra a URL na mão.
7. **Outras proteções:** consultas via Spring Data/JPA (parâmetros, sem concatenar SQL → sem injeção de SQL); cookie de sessão `HttpOnly` e `SameSite=Strict` (por isso o CSRF pôde ser desligado); sessão recriada no login (evita fixação de sessão); o front monta as tabelas com `textContent` (sem XSS); na listagem o CPF aparece parcialmente oculto (princípio da necessidade da LGPD); exportação CSV protegida contra injeção de fórmula.
8. **Conflito de horário:** validado na regra de negócio **e** por `UNIQUE` em `dataHoraAgendamento`, que barra duas pessoas agendando ao mesmo tempo.

## Dificuldades encontradas

- **Temporizador:** garantir um único contador (sem criar timers a cada evento) e que o servidor não expire antes do navegador quando o usuário só mexe o mouse; resolvido com um `ping` a cada 10 s enquanto há atividade e a folga de 5 s no servidor. Abas em segundo plano têm timers atrasados, então a contagem usa `Date.now()`.
- **Consultar dados cifrados:** a solução foi o hash HMAC do CPF (decisão de projeto descrita acima).
- **Chave de criptografia:** se for perdida, os dados cifrados ficam irrecuperáveis; é preciso guardar uma cópia da chave em local seguro, separado do banco.
- **Segurança × usabilidade:** 30 s é pouco para formulários longos; o aviso aos 25 s e o botão "Continuar conectado" amenizam, mas os dados digitados não são preservados após o redirecionamento.
- **Horário cancelado continua ocupando o horário** (a regra `UNIQUE` não diferencia status); para liberar, exclui-se o agendamento cancelado.
