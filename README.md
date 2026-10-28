# AutoBots — Microsserviço de Cadastro de Clientes (ATVII: RMM e HATEOAS)

API REST para o CRUD de clientes do sistema de gestão **AutoBots**, voltado a
lojas de manutenção veicular e venda de autopeças.

Primeiro microsserviço do projeto — os próximos (veículos, serviços, vendas)
serão containers independentes na mesma rede Docker.

**Atividade ATVII** — Prof. Dr. Eng. Gerson Penha

Continuação da ATVI. Nesta etapa a API passou a atender os quatro níveis do
modelo de maturidade de Richardson (RMM), incluindo o nível 3, **HATEOAS**: toda
resposta traz os links para os recursos relacionados. Veja
[Níveis de maturidade](#níveis-de-maturidade-rmm).

---

## Tecnologias

| | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 2.6.3 |
| Spring Data JPA / Hibernate | 5.6 |
| Spring HATEOAS | 1.4.1 |
| PostgreSQL | 16 |
| Docker Compose | — |

## O que precisa estar instalado na máquina

**Apenas o Docker Desktop** (ou Docker + Docker Compose no Linux).

Java, Maven e IDE **não** são necessários para executar. A compilação e a
execução acontecem dentro dos containers: o `Dockerfile` baixa o Maven, compila o
projeto e gera a imagem final contendo apenas o JRE e o `.jar`.

As portas `18080` (API) e `5433` (PostgreSQL) precisam estar livres.

---

## Como executar

```bash
git clone https://github.com/pedrodevroot/ATVII.git
cd ATVII
docker compose up -d --build
```

Nada para configurar: usuário, senha e nome do banco já estão no
`docker-compose.yml`.

| | Endereço |
|---|---|
| API | http://localhost:18080 |
| PostgreSQL | localhost:5433 — base/usuário/senha: `autobots` |

O comando sobe dois containers: o PostgreSQL e o microsserviço. Na primeira
subida, um cliente de demonstração é cadastrado automaticamente.

### Conferir se subiu

```bash
docker compose ps
curl http://localhost:18080/cliente/clientes
```

### Parar

```bash
docker compose down        # para os containers e mantém os dados
docker compose down -v     # para e apaga o banco
```

---

## Como executar os testes

```bash
./mvnw test          # Windows: .\mvnw.cmd test
```

São **196 testes automatizados**, em cerca de 20 segundos.

Os testes **não precisam de Docker nem de PostgreSQL** — os de integração usam
banco H2 em memória. Precisam apenas de **Java 17** instalado; o Maven vem junto
com o projeto, no `mvnw`.

| Camada | Testes | Ferramenta |
|---|---|---|
| Regras de domínio | 29 | JUnit 5 |
| Conversores DTO | 24 | JUnit 5 |
| Serviços | 46 | JUnit 5 + Mockito |
| Controllers (incluindo os links) | 74 | MockMvc |
| Mapeamento JPA | 12 | H2 |
| Integração ponta a ponta (incluindo a navegação pelos links) | 11 | H2 |

Para rodar os testes sem instalar Java, dentro de um container:

```bash
docker run --rm -v "${PWD}:/app" -w /app maven:3.8.4-eclipse-temurin-17 mvn test
```

> Com o VS Code (ou outra IDE Java) aberto no projeto, feche-o antes de rodar o
> comando acima: a IDE e o Maven do container compilam ao mesmo tempo na pasta
> `target` e corrompem as classes (`ClassFormatError`). Para rodar com a IDE
> aberta, use este comando, que compila uma cópia do código dentro do container
> e não toca na pasta `target`:
>
> ```bash
> docker run --rm -v "${PWD}:/src:ro" maven:3.8.4-eclipse-temurin-17 sh -c "mkdir /app && cp -r /src/pom.xml /src/src /app && cd /app && mvn test"
> ```

---

## Níveis de maturidade (RMM)

O modelo de maturidade de Richardson mede o quanto uma API segue o estilo REST.
Cada nível inclui o anterior.

| Nível | Significado | Como a API atende |
|---|---|---|
| **0** — Uso do protocolo HTTP | A comunicação acontece por HTTP | Toda a API é exposta por HTTP em `http://localhost:18080` |
| **1** — URI única por recurso | Cada recurso tem o seu próprio endereço | `/cliente/{id}`, `/telefone/{id}`, `/documento/{id}`, `/endereco/{id}` e as coleções `/cliente/{id}/telefones`, `/cliente/{id}/documentos`, `/cliente/{id}/enderecos` |
| **2** — Uso dos métodos e suas semânticas | O método HTTP diz a ação e o status diz o resultado | `GET` lê, `POST` cria, `PUT` atualiza, `DELETE` remove; respostas `200`, `201` + `Location`, `204`, `400`, `404` e `409` |
| **3** — HATEOAS | A resposta traz os links para os próximos passos | Toda resposta com corpo traz links gerados pelo Spring HATEOAS |

### Nível 3: HATEOAS

`GET http://localhost:18080/cliente/1` (trecho da resposta):

```json
{
  "id": 1,
  "nome": "Pedro Alcantara de Braganca e Bourbon",
  "nomeSocial": "Dom Pedro",
  "telefones": [ { "id": 1, "ddd": "21", "numero": "981234576" } ],
  "_links": {
    "self":       { "href": "http://localhost:18080/cliente/1" },
    "clientes":   { "href": "http://localhost:18080/cliente/clientes" },
    "telefones":  { "href": "http://localhost:18080/cliente/1/telefones" },
    "documentos": { "href": "http://localhost:18080/cliente/1/documentos" },
    "enderecos":  { "href": "http://localhost:18080/cliente/1/enderecos" }
  }
}
```

| Resposta | Links |
|---|---|
| Um cliente (`GET`, `POST`, `PUT`) | `self`, `clientes`, `telefones`, `documentos`, `enderecos` |
| Lista de clientes | `self` em cada cliente |
| Um telefone, documento ou endereço (`GET`, `POST`, `PUT`) | `self` |
| Lista de telefones, documentos ou endereços | `self` em cada item |
| `DELETE` (204) e respostas de erro | sem links |

Nas listas o Spring representa os links de cada item como
`"links": [ { "rel": "self", "href": "..." } ]`; num objeto único, como
`"_links": { "self": { "href": "..." } }` (formato HAL).

Com isso, um cliente da API precisa conhecer apenas o primeiro endereço. A partir
de `GET /cliente/1` ele segue `telefones`, depois o `self` de um telefone, e assim
por diante, sem montar nenhuma URL. O teste `navegacaoPelosLinks` do
`IntegracaoCrudTest` faz exatamente esse caminho.

**Como foi implementado:**

| Parte | Arquivo |
|---|---|
| Dependência `spring-boot-starter-hateoas` | `pom.xml` |
| DTOs de resposta estendem `RepresentationModel` | `ClienteRespostaDto`, `TelefoneDto`, `DocumentoDto`, `EnderecoDto` |
| Contrato dos adicionadores de link | `modelo/AdicionadorLink` |
| Montagem dos links com `WebMvcLinkBuilder` (`linkTo` + `methodOn`) | `modelo/AdicionadorLinkCliente`, `AdicionadorLinkTelefone`, `AdicionadorLinkDocumento`, `AdicionadorLinkEndereco` |
| Links adicionados antes de cada resposta | os 4 controllers |

Os links são construídos a partir dos métodos dos controllers, e não de textos
fixos: se uma rota mudar, o link muda junto.

### Escolhas de status no nível 2

O código de apoio da atividade usa outros status em dois casos. Aqui a escolha
foi seguir a semântica do HTTP:

| Situação | Código de apoio | Esta API | Motivo |
|---|---|---|---|
| `GET` com sucesso | `302 Found` | `200 OK` | `302` indica redirecionamento para outro endereço, não sucesso |
| Lista vazia | `404 Not Found` | `200 OK` com `[]` | A coleção existe; ela só não tem itens |
| `DELETE` com sucesso | `200 OK`, id no corpo da requisição | `204 No Content`, id na URL | Não há conteúdo a devolver, e o recurso é identificado pela URI |

---

## Endpoints

Base: `http://localhost:18080`

Em todo `POST` e `PUT` o corpo é JSON (`Content-Type: application/json`).

Toda resposta com corpo, menos as de erro, traz os links do recurso (veja
[Nível 3: HATEOAS](#nível-3-hateoas)).

Depois do `docker compose up`, o banco contém: cliente `1`, documentos `1` e `2`,
endereço `1` e telefone `1`.

### Cliente

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/cliente/clientes` | — | 200 |
| `GET` | `/cliente/1` | — | 200 |
| `POST` | `/cliente/cadastro` | **A** | 201 |
| `POST` | `/cliente/cadastro` | **B** | 201 |
| `PUT` | `/cliente/atualizar` | **C** | 200 |
| `DELETE` | `/cliente/excluir/2` | — | 204 |

### Telefone

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/cliente/1/telefones` | — | 200 |
| `POST` | `/cliente/1/telefones` | **D** | 201 |
| `GET` | `/telefone/1` | — | 200 |
| `PUT` | `/telefone/1` | `{ "ddd": "99" }` | 200 |
| `DELETE` | `/telefone/1` | — | 204 |

### Documento

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/cliente/1/documentos` | — | 200 |
| `POST` | `/cliente/1/documentos` | **E** | 201 |
| `GET` | `/documento/1` | — | 200 |
| `PUT` | `/documento/1` | `{ "tipo": "RG-novo" }` | 200 |
| `DELETE` | `/documento/1` | — | 204 |

### Endereço

Um cliente pode ter **vários** endereços.

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/cliente/1/enderecos` | — | 200 |
| `POST` | `/cliente/1/enderecos` | **F** | 201 |
| `GET` | `/endereco/1` | — | 200 |
| `PUT` | `/endereco/1` | `{ "cidade": "Niteroi" }` | 200 |
| `DELETE` | `/endereco/1` | — | 204 |

### Corpos

**A** — cadastro simples
```json
{ "nome": "Maria Silva" }
```

**B** — cadastro completo
```json
{
  "nome": "Joana Ferreira de Souza",
  "nomeSocial": "Joana",
  "dataNascimento": "1995-08-20T00:00:00.000+00:00",
  "documentos": [
    { "tipo": "RG", "numero": "998877" },
    { "tipo": "CPF", "numero": "12345678901" }
  ],
  "enderecos": [
    {
      "estado": "Sao Paulo", "cidade": "Santos", "bairro": "Gonzaga",
      "rua": "Avenida Ana Costa", "numero": "250",
      "codigoPostal": "11060002", "informacoesAdicionais": "Apartamento 32"
    }
  ],
  "telefones": [
    { "ddd": "13", "numero": "981234567" },
    { "ddd": "13", "numero": "32221100" }
  ]
}
```

**C** — atualização parcial: só os campos enviados são alterados. Para alterar um
filho, o `id` dele é obrigatório.
```json
{
  "id": 1,
  "nomeSocial": "Dom Pedro I",
  "enderecos": [ { "id": 1, "codigoPostal": "11015200" } ],
  "telefones": [ { "id": 1, "ddd": "11" } ]
}
```

**D** — telefone
```json
{ "ddd": "13", "numero": "988887777" }
```

**E** — documento
```json
{ "tipo": "CNH", "numero": "55443322" }
```

**F** — endereço
```json
{
  "estado": "Sao Paulo", "cidade": "Santos", "bairro": "Gonzaga",
  "rua": "Avenida Ana Costa", "numero": "250", "codigoPostal": "11060002"
}
```

> Os `id` nas URLs valem para o banco recém-criado. Cada `POST` devolve o `id`
> gerado no corpo da resposta e no header `Location` — é esse valor que deve ser
> usado nas requisições seguintes.

### Casos de erro

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/cliente/999` | — | 404 |
| `GET` | `/telefone/999` | — | 404 |
| `GET` | `/documento/999` | — | 404 |
| `GET` | `/endereco/999` | — | 404 |
| `DELETE` | `/cliente/excluir/999` | — | 404 |
| `POST` | `/cliente/999/telefones` | `{ "ddd": "13", "numero": "981234567" }` | 404 |
| `POST` | `/cliente/cadastro` | `{ "nomeSocial": "So apelido" }` | 400 |
| `POST` | `/cliente/cadastro` | `{ "nome": "X", "dataNascimento": "2099-01-01T00:00:00.000+00:00" }` | 400 |
| `POST` | `/cliente/cadastro` | **G** | 400 |
| `POST` | `/cliente/1/telefones` | `{ "ddd": "1", "numero": "abc" }` | 400 |
| `POST` | `/cliente/1/enderecos` | `{ "bairro": "Centro" }` | 400 |
| `PUT` | `/endereco/1` | `{ "codigoPostal": "123" }` | 400 |
| `PUT` | `/cliente/atualizar` | `{ "nome": "Sem Id" }` | 400 |
| `PUT` | `/cliente/atualizar` | `{ "id": 1, "telefones": [ { "ddd": "11" } ] }` | 400 |
| `POST` | `/cliente/cadastro` | **H** | 409 |
| `POST` | `/cliente/cadastro` | `{ "id": 1, "nome": "Tentativa" }` | 201, cria um novo |

**G** — vários campos inválidos ao mesmo tempo
```json
{
  "nome": "Invalido",
  "telefones": [ { "ddd": "1", "numero": "abc" } ],
  "enderecos": [ { "bairro": "Centro" } ],
  "documentos": [ { "tipo": "" } ]
}
```

**H** — número de documento duplicado (o CPF `00000000001` já pertence ao cliente 1)
```json
{ "nome": "Documento Repetido", "documentos": [ { "tipo": "CPF", "numero": "00000000001" } ] }
```

### Respostas de erro

```json
{
  "momento": "2026-09-27T18:01:30.026",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Cliente de id 999 nao foi encontrado",
  "caminho": "/cliente/999"
}
```

Erros de validação acrescentam a lista de campos, todos de uma vez:

```json
{
  "status": 400,
  "mensagem": "Os dados enviados nao passaram na validacao.",
  "erros": [
    { "campo": "nome", "mensagem": "O nome e obrigatorio" },
    { "campo": "telefones[0].ddd", "mensagem": "O DDD deve conter exatamente 2 digitos" },
    { "campo": "enderecos[0].cidade", "mensagem": "A cidade e obrigatoria" }
  ]
}
```

### Validações

| Campo | Regra |
|---|---|
| `cliente.nome` | obrigatório no cadastro |
| `cliente.dataNascimento` | se informada, deve estar no passado |
| `cliente.dataCadastro` | definido pelo servidor, não aceito na entrada |
| `documento.tipo` · `numero` | obrigatórios; `numero` é único no sistema |
| `telefone.ddd` | 2 dígitos |
| `telefone.numero` | 8 ou 9 dígitos |
| `endereco.cidade` · `rua` · `numero` | obrigatórios |
| `endereco.codigoPostal` | se informado, 8 dígitos |
| `id` dos filhos na atualização | obrigatório |

---

## Alterações da ATVII

| O que mudou | Onde |
|---|---|
| Dependência do Spring HATEOAS | `pom.xml` |
| DTOs de resposta passaram a carregar links | `ClienteRespostaDto`, `TelefoneDto`, `DocumentoDto`, `EnderecoDto` |
| Interface e adicionadores de link | `AdicionadorLink` e os 4 `AdicionadorLink*` em `modelo` |
| Respostas de `GET`, `POST` e `PUT` com links | `ClienteControle`, `TelefoneControle`, `DocumentoControle`, `EnderecoControle` |
| 21 testes novos: links de cada resposta e navegação pelos links | os 4 testes de controller e `IntegracaoCrudTest` |

Rotas, corpos, validações e status continuam os mesmos da ATVI.

## Alterações em relação ao código inicial da ATVI

| Problema | Correção |
|---|---|
| `Long` comparado com `==` nos atualizadores — compara referência, não valor. Funcionava para ids de 1 a 127 (cache de `Long` da JVM) e falhava silenciosamente a partir de 128 | `.equals()` |
| Prefixo repetido gerava a rota `/cliente/cliente/{id}` | `@GetMapping("/{id}")` |
| `codigoPostal` não era atualizado — o CEP era imutável pela API | bloco acrescentado ao `EnderecoAtualizador` |
| `NullPointerException` no `EnderecoAtualizador` quando o endereço existente era nulo | guarda contra nulo |
| `POST /cadastro` aceitava `id` e o `save()` fazia *upsert* — era possível sobrescrever um cliente existente | `id` removido do DTO de cadastro |
| `dataCadastro` ficava nula quando não enviada | definida pelo `ClienteServico` |
| Listagens sem `ORDER BY` — a mesma requisição podia devolver ordens diferentes | `findAll(Sort.by("id"))` e `@OrderBy("id")` nas coleções |
