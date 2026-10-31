# Estação Pet — Sistema de Gestão para Pet Shop

Aplicação web (Spring Boot + MySQL + HTML/CSS/JS) para controlar tutores, pets, agendamentos de banho e tosa e produtos, com login, senhas com hash, dados pessoais criptografados e sessão que expira após **30 segundos** de inatividade.

## Funcionalidades
- Login/logout, alteração de senha e perfis **ADMIN** e **ATENDENTE** (somente ADMIN vê e acessa *Funcionários*).
- CRUD de **tutores** (nome, telefone, e-mail, CPF, endereço), **pets**, **produtos** e **funcionários**, com busca por nome e confirmação antes de excluir.
- **Agendamentos** (banho, tosa ou ambos) com data, horário, pet e status (agendado, em andamento, concluído, cancelado). Horários de 30 em 30 min, das 08:00 às 17:30; não permite dois no mesmo horário nem datas passadas.
- **Painel inicial** com agendamentos do dia e total de clientes.
- Bônus feitos: perfis de acesso, aviso aos 25 s antes de expirar, exportação CSV dos agendamentos listados e teste automatizado do CPF.

## Requisitos
Java 25, Maven (ou o do IntelliJ) e MySQL 8+.

## Como executar
1. **Banco:** execute o script `estacao_pet.sql` no MySQL (cria o banco `ESTACAO_PET` e as tabelas).
2. **Variáveis de ambiente** (não ficam no código):

| Variável | Para quê | Obrigatória |
|---|---|---|
| `ESTACAO_CHAVE` | Segredo usado para criptografar os dados (use uma frase longa e aleatória) | **Sim** — sem ela a aplicação não inicia |
| `ADMIN_LOGIN` | E-mail do administrador inicial (ex.: `admin@estacaopet.com`) | Só no 1º uso |
| `ADMIN_SENHA` | Senha do administrador inicial (mínimo 6 caracteres) | Só no 1º uso |
| `ADMIN_NOME` | Nome do administrador (padrão: Administrador) | Não |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexão com o MySQL (padrão: `localhost:3306/ESTACAO_PET`, usuário `root`, senha vazia) | Se o seu MySQL for diferente |

   - **IntelliJ:** *Run > Edit Configurations > EstacaoPetApplication > Environment variables* e informe `ESTACAO_CHAVE=...;ADMIN_LOGIN=...;ADMIN_SENHA=...`.
   - **PowerShell:** `$env:ESTACAO_CHAVE="sua-frase-secreta"; $env:ADMIN_LOGIN="admin@estacaopet.com"; $env:ADMIN_SENHA="123456"; ./mvnw spring-boot:run`
   - **Linux/macOS:** `export ESTACAO_CHAVE=...` e depois `./mvnw spring-boot:run`.
3. Abra **http://localhost:8080** e entre com o administrador. O administrador só é criado se a tabela `usuarios` estiver vazia.

> ⚠️ **Guarde uma cópia da `ESTACAO_CHAVE` em local seguro, fora do banco e do repositório.** Se ela for perdida (ou trocada), CPF, telefone, e-mail e endereço já gravados não podem mais ser recuperados.

## Estrutura do projeto
```
src/main/java/estacaoPet/
  controller/   Controllers REST + TratadorErros (erros em JSON)
  Service/      Regras de negócio, validações e DTOs
  model/        Entidades JPA (Tutor traz o conversor AES-GCM)
  repository/   Spring Data JPA
  Secutiry/     PasswordSecurity (BCrypt) e SessaoConfig (sessão no servidor)
src/main/resources/
  application.properties
  static/
    index.html        redireciona para html/login.html
    html/             telas (login, home, tutores, pets, agendamentos, produtos, funcionários, perfil)
    css/              estilos (comum.css = mensagens e erros dos formulários)
    js/               auth.js, comum.js (API e utilidades), inatividade.js, header.js e um script por tela
src/test/java/        TutorServiceTest (validação de CPF)
docs/                 DIAGRAMA_ER.md e RELATORIO.md
```
Ordem dos scripts em cada tela: `auth.js` (no `<head>`) → `comum.js` → `header.js` → `inatividade.js` → script da tela.

## API (resumo)
Tudo em `/api/**` exige login (401 sem sessão ou com sessão expirada), exceto `POST /api/login` e `POST /api/logout`.

| Recurso | Rotas |
|---|---|
| Sessão | `POST /login`, `POST /logout`, `GET /sessao/ping`, `GET /me`, `PUT /me/senha` |
| Tutores | `GET/POST /tutores`, `GET/PUT/DELETE /tutores/{id}` (`?busca=` nome ou CPF exato) |
| Pets | `GET/POST /pets`, `GET/PUT/DELETE /pets/{id}` (`?busca=` pet ou tutor) |
| Agendamentos | `GET/POST /agendamentos` (`?data=&status=`), `GET /agendamentos/horarios?data=`, `GET/PUT/DELETE /agendamentos/{id}`, `PATCH /agendamentos/{id}/status` |
| Produtos | `GET/POST /produtos`, `GET/PUT/DELETE /produtos/{id}` |
| Funcionários (ADMIN) | `GET/POST /funcionarios`, `DELETE /funcionarios/{id}` |
| Painel | `GET /painel` |

## Testes manuais sugeridos
1. **Login:** senha errada mostra "Usuário ou senha inválidos."; certa leva ao painel.
2. **Expiração:** fique 25 s sem mexer em nada → aparece o aviso; aos 30 s a tela é bloqueada e você volta ao login com a mensagem de sessão expirada. Mover o mouse, clicar, digitar ou rolar reinicia a contagem.
3. **Servidor valida sozinho:** entre, espere 40 s sem usar o navegador (ou pause o JavaScript nas ferramentas do desenvolvedor) e chame `/api/tutores` → responde **401** `SESSAO_EXPIRADA`.
4. **Perfil:** entre como ATENDENTE → o menu não mostra *Funcionários* e `/api/funcionarios` responde **403**.
5. **Banco sem dados legíveis:**
   ```sql
   SELECT loginUsuario, senhaHashUsuario FROM usuarios;           -- hashes $2a$..., nunca a senha
   SELECT nomeTutor, HEX(cpfCifTutor), cpfHashTutor FROM tutor;   -- CPF aparece cifrado
   ```
6. **Conflito:** tente agendar dois pets no mesmo dia/horário (o horário some da lista; se duas pessoas tentarem juntas, o `UNIQUE` do banco barra).
7. **Validações:** CPF inválido/duplicado, telefone com menos de 10 dígitos, data no passado, campos vazios — cada erro aparece junto do campo.
8. **Testes automatizados:** `./mvnw test -Dtest=TutorServiceTest`.

## Segurança e LGPD (resumo)
Detalhes no [relatório](docs/RELATORIO.md) e no [diagrama ER](../../Downloads/vibecod/novos/docs/DIAGRAMA_ER.md). Em resumo: BCrypt para senhas; AES-GCM para dados pessoais do tutor; HMAC para buscar CPF; chave só em variável de ambiente; validação de sessão em cada requisição; consultas parametrizadas (sem injeção de SQL); coleta limitada ao necessário (necessidade e finalidade) e CPF parcialmente oculto nas listagens.