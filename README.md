# AutoBots — Automanager (ATVIII: atualização de base)

API REST do **Automanager**, sistema de gestão da AutoBots para lojas de
manutenção veicular e venda de autopeças.

**Atividade ATVIII** — Prof. Dr. Eng. Gerson Penha

Continuação da ATVII. Nesta etapa o sistema deixou de cadastrar apenas clientes e
passou a registrar empresas, usuários, veículos, mercadorias, serviços e vendas,
com CRUD e HATEOAS em todas as entidades.

| Funcionalidade pedida | Onde está |
|---|---|
| Cadastro de empresa | [`/empresa`](#empresa) |
| Associação de usuários a uma empresa | [`/empresa/{id}/usuarios`](#empresa) |
| Tipos de usuário: cliente, fornecedor ou funcionário | campo `perfis` em [`/usuario`](#usuário) |
| Cadastro de veículos, mercadorias e serviços, para gerenciar vendas e serviços prestados a um usuário e seu veículo | [`/veiculo`](#veículo), [`/mercadoria`](#mercadoria), [`/servico`](#serviço), [`/venda`](#venda) |
| Credencial de acesso do usuário ao sistema | [`/credencial`](#credencial) |

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
git clone https://github.com/pedrodevroot/ATVIII.git
cd ATVIII
docker compose up -d --build
```

Nada para configurar: usuário, senha e nome do banco já estão no
`docker-compose.yml`.

| | Endereço |
|---|---|
| API | http://localhost:18080 |
| PostgreSQL | localhost:5433 — base/usuário/senha: `autobots` |

O comando sobe dois containers: o PostgreSQL e o microsserviço. Na primeira
subida, os [dados de demonstração](#dados-de-demonstração) são cadastrados
automaticamente.

> **Já executou a ATVII nesta máquina?** As duas atividades usam o mesmo volume
> do banco, e o esquema antigo impede a aplicação de subir (erro na coluna
> `data_emissao`). Antes do `up`, apague esse banco e o container antigo com
> `docker compose down -v --remove-orphans`.
> O comando remove de vez os dados que estiverem nele.

### Conferir se subiu

```bash
docker compose ps
curl http://localhost:18080/usuario/usuarios
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

São **508 testes automatizados**.

Os testes **não precisam de Docker nem de PostgreSQL** — os de integração usam
banco H2 em memória. Precisam apenas de **Java 17** ou mais recente instalado; o
Maven vem junto com o projeto, no `mvnw`.

| Camada | Testes | Ferramenta |
|---|---|---|
| Regras de domínio | 50 | JUnit 5 |
| Conversores DTO | 51 | JUnit 5 |
| Serviços | 148 | JUnit 5 + Mockito |
| Controllers (incluindo os links) | 208 | MockMvc |
| Mapeamento JPA | 21 | H2 |
| Integração ponta a ponta (incluindo a navegação pelos links) | 30 | H2 |

Para rodar os testes sem instalar Java, dentro de um container:

```bash
docker run --rm -v "${PWD}:/src:ro" maven:3.8.4-eclipse-temurin-17 sh -c "mkdir /app && cp -r /src/pom.xml /src/src /app && cd /app && mvn test"
```

O comando compila uma cópia do código dentro do container e não toca na pasta
`target`, então pode ser usado com a IDE aberta.

---

## Modelo de dados

As classes seguem o diagrama da Figura 1 do enunciado.

| Entidade | O que representa | Ligações |
|---|---|---|
| `Empresa` | Unidade comercial que oferece os serviços e vende as mercadorias | endereço, telefones, usuários, mercadorias, serviços e vendas |
| `Usuario` | Cliente, fornecedor ou funcionário (`perfis`) | empresa, endereço, telefones, documentos, e-mails, credenciais, veículos, mercadorias fornecidas e vendas |
| `Credencial` | Acesso ao sistema: `CredencialUsuarioSenha` ou `CredencialCodigoBarra` | usuário |
| `Veiculo` | Carro de um usuário: tipo, modelo e placa | proprietário e vendas |
| `Mercadoria` | Peça ou produto: validade, fabricação, quantidade e valor | empresa e fornecedor |
| `Servico` | Trabalho oferecido pela oficina: nome, valor e descrição | empresa |
| `Venda` | Registro do que foi vendido a um cliente | cliente, funcionário, veículo (opcional), mercadorias e serviços |

| Enumeração | Valores |
|---|---|
| `PerfilUsuario` | `CLIENTE`, `FUNCIONARIO`, `FORNECEDOR` |
| `TipoDocumento` | `CPF`, `CNPJ`, `RG`, `CNH`, `PASSAPORTE` |
| `TipoVeiculo` | `HATCH`, `SEDA`, `SUV`, `PICKUP`, `SW` |

### Alterações em relação ao código sugerido

O enunciado dá liberdade para modificar o código da equipe. As funcionalidades
foram mantidas, com estas mudanças:

| Código sugerido | Este projeto | Motivo |
|---|---|---|
| `Set` com `fetch = EAGER` nas coleções | `List` com carga sob demanda e `@OrderBy("id")` | Cada consulta não carrega o banco inteiro, e as listagens têm ordem fixa |
| `double` em `valor` | `BigDecimal` | `double` acumula erro de arredondamento em valores monetários |
| `@OneToMany` de venda para mercadorias e serviços | `@ManyToMany` | Com `@OneToMany`, a mesma mercadoria ou serviço não pode aparecer em duas vendas |
| Validade obrigatória em `Mercadoria` | Validade opcional | Peças como rodas não têm validade |
| Placa sem restrição | Placa única | Dois veículos não têm a mesma placa |
| Pacote `entitades`, atributo `fabricao` | `entidades`, `fabricacao` | Correção de grafia |

A classe `Cliente` das atividades anteriores foi substituída por `Usuario`.

---

## Níveis de maturidade (RMM)

O modelo de maturidade de Richardson mede o quanto uma API segue o estilo REST.
Cada nível inclui o anterior.

| Nível | Significado | Como a API atende |
|---|---|---|
| **0** — Uso do protocolo HTTP | A comunicação acontece por HTTP | Toda a API é exposta por HTTP em `http://localhost:18080` |
| **1** — URI única por recurso | Cada recurso tem o seu próprio endereço | `/usuario/{id}`, `/empresa/{id}`, `/veiculo/{id}`, `/venda/{id}` e as coleções `/usuario/{id}/veiculos`, `/empresa/{id}/vendas` |
| **2** — Uso dos métodos e suas semânticas | O método HTTP diz a ação e o status diz o resultado | `GET` lê, `POST` cria, `PUT` atualiza, `DELETE` remove; respostas `200`, `201` + `Location`, `204`, `400`, `404` e `409` |
| **3** — HATEOAS | A resposta traz os links para os próximos passos | Toda resposta com corpo traz links gerados pelo Spring HATEOAS |

### Nível 3: HATEOAS

`GET http://localhost:18080/venda/1` (trecho da resposta):

```json
{
  "id": 1,
  "identificacao": "1234698745",
  "clienteId": 3,
  "funcionarioId": 1,
  "veiculoId": 1,
  "mercadorias": [
    {
      "id": 1,
      "nome": "Roda de liga leve modelo toyota etios",
      "quantidade": 30,
      "valor": 300.00,
      "_links": { "self": { "href": "http://localhost:18080/mercadoria/1" } }
    }
  ],
  "_links": {
    "self":        { "href": "http://localhost:18080/venda/1" },
    "cliente":     { "href": "http://localhost:18080/usuario/3" },
    "funcionario": { "href": "http://localhost:18080/usuario/1" },
    "veiculo":     { "href": "http://localhost:18080/veiculo/1" }
  }
}
```

| Resposta | Links |
|---|---|
| Uma empresa | `self`, `empresas`, `usuarios`, `telefones`, `mercadorias`, `servicos`, `vendas` e, se houver, `endereco` |
| Um usuário | `self`, `usuarios`, `telefones`, `documentos`, `emails`, `credenciais`, `veiculos`, `mercadorias`, `vendas` e, se houver, `endereco` e `empresa` |
| Um veículo | `self`, `proprietario`, `vendas` |
| Uma venda | `self`, `cliente`, `funcionario` e, se houver, `veiculo`; `self` em cada mercadoria e serviço |
| Uma mercadoria, serviço, telefone, documento, e-mail, endereço ou credencial | `self` |
| Qualquer lista | `self` em cada item |
| `PUT` de vínculo, `DELETE` (204) e respostas de erro | sem links |

Nas listas o Spring representa os links de cada item como
`"links": [ { "rel": "self", "href": "..." } ]`; num objeto único, como
`"_links": { "self": { "href": "..." } }` (formato HAL).

Com isso, um cliente da API precisa conhecer apenas o primeiro endereço. A partir
de `GET /empresa/empresas` ele chega à empresa, às vendas dela, ao veículo da
venda e ao proprietário do veículo sem montar nenhuma URL. O teste
`navegacaoAteAVenda` do `IntegracaoVendaTest` faz exatamente esse caminho.

**Como foi implementado:**

| Parte | Arquivo |
|---|---|
| Dependência `spring-boot-starter-hateoas` | `pom.xml` |
| DTOs de resposta estendem `RepresentationModel` | `VendaRespostaDto`, `VeiculoDto`, `MercadoriaDto`, `ServicoDto` e os demais |
| Contrato dos adicionadores de link | `modelo/AdicionadorLink` |
| Montagem dos links com `WebMvcLinkBuilder` (`linkTo` + `methodOn`) | `modelo/AdicionadorLink*` |
| Links adicionados antes de cada resposta | os controllers |

Os links são construídos a partir dos métodos dos controllers, e não de textos
fixos: se uma rota mudar, o link muda junto.

### Escolhas de status no nível 2

O código de apoio das atividades usa outros status em alguns casos. Aqui a escolha
foi seguir a semântica do HTTP:

| Situação | Código de apoio | Esta API | Motivo |
|---|---|---|---|
| `GET` com sucesso | `302 Found` | `200 OK` | `302` indica redirecionamento para outro endereço, não sucesso |
| Lista vazia | `404 Not Found` | `200 OK` com `[]` | A coleção existe; ela só não tem itens |
| `DELETE` com sucesso | `200 OK`, id no corpo da requisição | `204 No Content`, id na URL | Não há conteúdo a devolver, e o recurso é identificado pela URI |

---

## Dados de demonstração

Cadastrados automaticamente quando o banco está vazio:

| Recurso | Ids |
|---|---|
| Empresa | `1` — Car service toyota ltda |
| Usuários | `1` funcionário, `2` fornecedor, `3` cliente, todos vinculados à empresa `1` |
| Veículo | `1` — Corolla Cross, placa `ABC0000`, do cliente `3` |
| Mercadoria | `1` — roda de liga leve, fornecida pelo usuário `2` |
| Serviços | `1` troca de rodas, `2` alinhamento, `3` balanceamento |
| Vendas | `1` e `2` — cliente `3`, funcionário `1`, veículo `1` |
| Credenciais | `1` `dompedrofuncionario`, `2` `dompedrofornecedor`, `3` `dompedrocliente`, todas com a senha `123456` |
| Documentos | `1`, `2`, `3` — um para cada usuário, na mesma ordem |
| E-mails | `1`, `2`, `3` — um para cada usuário, na mesma ordem |
| Endereços | `1`, `2`, `3` dos usuários, na mesma ordem, e `4` da empresa |
| Telefones | `1` do usuário `1` e `2` da empresa |

---

## Endpoints

Base: `http://localhost:18080`

Em todo `POST` e `PUT` com corpo, o conteúdo é JSON
(`Content-Type: application/json`).

As tabelas abaixo podem ser executadas de cima para baixo em um banco recém-criado.
Os `id` nas URLs valem para essa ordem. Cada `POST` devolve o `id` gerado no corpo
da resposta e no header `Location` — em outra ordem, é esse valor que deve ser usado.

### Usuário

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/usuario/usuarios` | — | 200 |
| `GET` | `/usuario/3` | — | 200 |
| `POST` | `/usuario/cadastro` | **A** | 201 |
| `POST` | `/usuario/cadastro` | **B** | 201 |
| `PUT` | `/usuario/atualizar` | **C** | 200 |
| `DELETE` | `/usuario/excluir/4` | — | 204 |

### Empresa

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/empresa/empresas` | — | 200 |
| `GET` | `/empresa/1` | — | 200 |
| `POST` | `/empresa/cadastro` | **D** | 201 |
| `PUT` | `/empresa/atualizar` | **E** | 200 |
| `GET` | `/empresa/1/usuarios` | — | 200 |
| `PUT` | `/empresa/2/usuarios/5` | — | 204 |
| `GET` | `/empresa/2/usuarios` | — | 200 |
| `DELETE` | `/empresa/2/usuarios/5` | — | 204 |
| `DELETE` | `/empresa/excluir/2` | — | 204 |

Um usuário fica vinculado a uma empresa por vez. A exclusão da empresa apaga as
vendas, mercadorias e serviços dela e mantém os usuários, sem vínculo.

### Veículo

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/usuario/3/veiculos` | — | 200 |
| `POST` | `/usuario/3/veiculos` | **L** | 201 |
| `GET` | `/veiculo/2` | — | 200 |
| `PUT` | `/veiculo/2` | `{ "modelo": "Etios X" }` | 200 |
| `DELETE` | `/veiculo/2` | — | 204 |

### Mercadoria

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/empresa/1/mercadorias` | — | 200 |
| `POST` | `/empresa/1/mercadorias` | **M** | 201 |
| `GET` | `/mercadoria/2` | — | 200 |
| `PUT` | `/mercadoria/2` | `{ "quantidade": 8 }` | 200 |
| `GET` | `/usuario/2/mercadorias` | — | 200 |
| `PUT` | `/usuario/2/mercadorias/2` | — | 204 |
| `DELETE` | `/usuario/2/mercadorias/2` | — | 204 |
| `DELETE` | `/mercadoria/2` | — | 204 |

`/usuario/{id}/mercadorias` são as mercadorias fornecidas por aquele usuário. O
`PUT` vincula o fornecedor à mercadoria, e o `DELETE` desfaz o vínculo sem apagar
a mercadoria.

### Serviço

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/empresa/1/servicos` | — | 200 |
| `POST` | `/empresa/1/servicos` | **N** | 201 |
| `GET` | `/servico/4` | — | 200 |
| `PUT` | `/servico/4` | `{ "valor": 250.00 }` | 200 |
| `DELETE` | `/servico/4` | — | 204 |

### Venda

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/empresa/1/vendas` | — | 200 |
| `GET` | `/usuario/3/vendas` | — | 200 |
| `GET` | `/veiculo/1/vendas` | — | 200 |
| `POST` | `/empresa/1/vendas` | **O** | 201 |
| `GET` | `/venda/3` | — | 200 |
| `PUT` | `/venda/3` | **P** | 200 |
| `DELETE` | `/venda/3` | — | 204 |

`/usuario/{id}/vendas` traz as vendas em que o usuário é o cliente ou o funcionário.

No `PUT`, só os campos enviados são alterados. Uma lista enviada substitui a
anterior, e uma lista vazia remove os itens, desde que a venda continue com ao
menos um item.

### Telefone

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/usuario/1/telefones` | — | 200 |
| `POST` | `/usuario/1/telefones` | **F** | 201 |
| `GET` | `/empresa/1/telefones` | — | 200 |
| `POST` | `/empresa/1/telefones` | **F** | 201 |
| `GET` | `/telefone/1` | — | 200 |
| `PUT` | `/telefone/1` | `{ "ddd": "12" }` | 200 |
| `DELETE` | `/telefone/1` | — | 204 |

### Documento

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/usuario/1/documentos` | — | 200 |
| `POST` | `/usuario/1/documentos` | **G** | 201 |
| `GET` | `/documento/1` | — | 200 |
| `PUT` | `/documento/1` | `{ "tipo": "RG" }` | 200 |
| `DELETE` | `/documento/1` | — | 204 |

### E-mail

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/usuario/1/emails` | — | 200 |
| `POST` | `/usuario/1/emails` | **H** | 201 |
| `GET` | `/email/1` | — | 200 |
| `PUT` | `/email/1` | `{ "endereco": "novo@email.com" }` | 200 |
| `DELETE` | `/email/1` | — | 204 |

### Credencial

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/usuario/1/credenciais` | — | 200 |
| `POST` | `/usuario/1/credenciais` | **I** | 201 |
| `POST` | `/usuario/1/credenciais` | **J** | 201 |
| `GET` | `/credencial/1` | — | 200 |
| `PUT` | `/credencial/1` | `{ "inativo": true }` | 200 |
| `DELETE` | `/credencial/1` | — | 204 |

O tipo é informado no campo `tipo`: `USUARIO_SENHA` ou `CODIGO_BARRA`. A senha
nunca aparece nas respostas.

### Endereço

Um usuário ou uma empresa tem **um** endereço.

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/usuario/1/endereco` | — | 200 |
| `GET` | `/empresa/1/endereco` | — | 200 |
| `GET` | `/endereco/1` | — | 200 |
| `PUT` | `/endereco/1` | `{ "cidade": "Santos" }` | 200 |
| `DELETE` | `/endereco/1` | — | 204 |
| `POST` | `/usuario/1/endereco` | **K** | 201 |

### Corpos

**A** — usuário simples
```json
{ "nome": "Maria Silva", "perfis": [ "CLIENTE" ] }
```

**B** — usuário completo
```json
{
  "nome": "Joana Ferreira de Souza",
  "nomeSocial": "Joana",
  "dataNascimento": "1995-08-20T00:00:00.000+00:00",
  "perfis": [ "CLIENTE" ],
  "endereco": {
    "estado": "Sao Paulo", "cidade": "Santos", "bairro": "Gonzaga",
    "rua": "Avenida Ana Costa", "numero": "250",
    "codigoPostal": "11060002", "informacoesAdicionais": "Apartamento 32"
  },
  "documentos": [
    { "tipo": "RG", "dataEmissao": "2015-03-10T00:00:00.000+00:00", "numero": "998877" },
    { "tipo": "CPF", "dataEmissao": "2010-05-20T00:00:00.000+00:00", "numero": "12345678901" }
  ],
  "telefones": [ { "ddd": "13", "numero": "981234567" } ],
  "emails": [ { "endereco": "joana@email.com" } ],
  "credenciais": [
    { "tipo": "USUARIO_SENHA", "nomeUsuario": "joana", "senha": "segredo1" },
    { "tipo": "CODIGO_BARRA", "codigo": 7891234 }
  ]
}
```

**C** — atualização parcial do usuário: só os campos enviados são alterados. Para
alterar um filho, o `id` dele é obrigatório.
```json
{
  "id": 3,
  "nomeSocial": "Dom Pedro I",
  "endereco": { "codigoPostal": "11015200" },
  "emails": [ { "id": 3, "endereco": "dompedro@email.com" } ]
}
```

**D** — empresa
```json
{
  "razaoSocial": "Volkswagen Servicos Ltda",
  "nomeFantasia": "VW Servicos",
  "endereco": { "cidade": "Santos", "rua": "Avenida Ana Costa", "numero": "100" },
  "telefones": [ { "ddd": "13", "numero": "32221100" } ]
}
```

**E** — atualização parcial da empresa
```json
{ "id": 2, "nomeFantasia": "VW Santos" }
```

**F** — telefone
```json
{ "ddd": "13", "numero": "988887777" }
```

**G** — documento
```json
{ "tipo": "CNH", "dataEmissao": "2020-01-15T00:00:00.000+00:00", "numero": "55443322" }
```

**H** — e-mail
```json
{ "endereco": "maria@email.com" }
```

**I** — credencial de usuário e senha
```json
{ "tipo": "USUARIO_SENHA", "nomeUsuario": "maria", "senha": "segredo1" }
```

**J** — credencial de código de barras
```json
{ "tipo": "CODIGO_BARRA", "codigo": 123456789 }
```

**K** — endereço
```json
{
  "estado": "Sao Paulo", "cidade": "Santos", "bairro": "Gonzaga",
  "rua": "Avenida Ana Costa", "numero": "250", "codigoPostal": "11060002"
}
```

**L** — veículo
```json
{ "tipo": "HATCH", "modelo": "Etios", "placa": "XYZ1A23" }
```

**M** — mercadoria
```json
{
  "nome": "Oleo de motor",
  "descricao": "Oleo sintetico 5W30",
  "fabricacao": "2025-03-01T00:00:00.000+00:00",
  "validade": "2027-03-01T00:00:00.000+00:00",
  "quantidade": 5,
  "valor": 49.90
}
```

**N** — serviço
```json
{ "nome": "Revisao", "descricao": "Revisao completa", "valor": 200.00 }
```

**O** — venda
```json
{
  "identificacao": "OS-0001",
  "clienteId": 3,
  "funcionarioId": 1,
  "veiculoId": 1,
  "mercadoriaIds": [ 1 ],
  "servicoIds": [ 1, 2 ]
}
```

**P** — atualização parcial da venda
```json
{ "identificacao": "OS-0002", "servicoIds": [ 3 ] }
```

### Casos de erro

Executados depois das tabelas acima.

| Método | URL | Corpo | Esperado |
|---|---|---|---|
| `GET` | `/usuario/999` | — | 404 |
| `GET` | `/empresa/999` | — | 404 |
| `GET` | `/veiculo/999` | — | 404 |
| `GET` | `/mercadoria/999` | — | 404 |
| `GET` | `/servico/999` | — | 404 |
| `GET` | `/venda/999` | — | 404 |
| `DELETE` | `/usuario/excluir/999` | — | 404 |
| `POST` | `/usuario/999/veiculos` | **L** | 404 |
| `POST` | `/empresa/999/vendas` | **O** | 404 |
| `POST` | `/usuario/cadastro` | `{ "nomeSocial": "Sem nome" }` | 400 |
| `POST` | `/usuario/cadastro` | `{ "nome": "Sem perfil" }` | 400 |
| `POST` | `/usuario/cadastro` | `{ "nome": "X", "perfis": [ "GERENTE" ] }` | 400 |
| `POST` | `/usuario/cadastro` | **Q** | 400 |
| `PUT` | `/usuario/atualizar` | `{ "nome": "Sem id" }` | 400 |
| `PUT` | `/usuario/atualizar` | `{ "id": 3, "perfis": [ null ] }` | 400 |
| `POST` | `/usuario/cadastro` | `{ "nome": "X", "perfis": [ "CLIENTE" ], "telefones": [ null ] }` | 400 |
| `POST` | `/usuario/cadastro` | `{ "nome": "X", "perfis": [ 0 ] }` | 400 |
| `POST` | `/empresa/cadastro` | `{ "nomeFantasia": "Sem razao social" }` | 400 |
| `POST` | `/usuario/3/veiculos` | `{ "tipo": "SUV", "modelo": "Yaris", "placa": "abc" }` | 400 |
| `POST` | `/usuario/3/veiculos` | `{ "tipo": "AVIAO", "modelo": "Yaris", "placa": "XYZ1234" }` | 400 |
| `POST` | `/usuario/3/veiculos` | `{ "tipo": 0, "modelo": "Yaris", "placa": "XYZ1234" }` | 400 |
| `POST` | `/empresa/1/mercadorias` | **R** | 400 |
| `POST` | `/empresa/1/servicos` | `{ "nome": "Negativo", "valor": -1 }` | 400 |
| `PUT` | `/usuario/3/mercadorias/1` | — | 400 |
| `POST` | `/empresa/1/vendas` | **S** | 400 |
| `POST` | `/empresa/1/vendas` | **T** | 400 |
| `POST` | `/empresa/1/vendas` | **U** | 400 |
| `POST` | `/empresa/1/vendas` | **V** | 400 |
| `POST` | `/empresa/1/vendas` | **W** | 400 |
| `POST` | `/usuario/cadastro` | **X** | 409 |
| `POST` | `/usuario/3/veiculos` | `{ "tipo": "SUV", "modelo": "Yaris", "placa": "ABC0000" }` | 409 |
| `POST` | `/usuario/1/endereco` | **K** | 409 |
| `POST` | `/empresa/1/vendas` | **Y** | 409 |
| `DELETE` | `/usuario/excluir/3` | — | 409 |
| `DELETE` | `/veiculo/1` | — | 409 |
| `DELETE` | `/mercadoria/1` | — | 409 |
| `DELETE` | `/servico/1` | — | 409 |

**Q** — vários campos inválidos ao mesmo tempo
```json
{
  "nome": "Invalido",
  "perfis": [ "CLIENTE" ],
  "telefones": [ { "ddd": "1", "numero": "abc" } ],
  "endereco": { "bairro": "Centro" },
  "emails": [ { "endereco": "sem arroba" } ]
}
```

**R** — validade anterior à fabricação
```json
{
  "nome": "Oleo",
  "fabricacao": "2025-03-01T00:00:00.000+00:00",
  "validade": "2020-03-01T00:00:00.000+00:00",
  "quantidade": 5,
  "valor": 49.90
}
```

**S** — o usuário `1` é funcionário, não cliente
```json
{ "identificacao": "E-1", "clienteId": 1, "funcionarioId": 1, "servicoIds": [ 1 ] }
```

**T** — o usuário `3` é cliente, não funcionário
```json
{ "identificacao": "E-2", "clienteId": 3, "funcionarioId": 3, "servicoIds": [ 1 ] }
```

**U** — o veículo `1` não é do cliente `5`
```json
{ "identificacao": "E-3", "clienteId": 5, "funcionarioId": 1, "veiculoId": 1, "servicoIds": [ 1 ] }
```

**V** — venda sem nenhum item
```json
{ "identificacao": "E-4", "clienteId": 3, "funcionarioId": 1 }
```

**W** — o mesmo item repetido
```json
{ "identificacao": "E-5", "clienteId": 3, "funcionarioId": 1, "servicoIds": [ 1, 1 ] }
```

**X** — número de documento duplicado (o CPF `12584698533` já pertence ao usuário `3`)
```json
{
  "nome": "Documento Repetido",
  "perfis": [ "CLIENTE" ],
  "documentos": [
    { "tipo": "CPF", "dataEmissao": "2010-05-20T00:00:00.000+00:00", "numero": "12584698533" }
  ]
}
```

**Y** — identificação de venda duplicada
```json
{ "identificacao": "1234698745", "clienteId": 3, "funcionarioId": 1, "servicoIds": [ 1 ] }
```

### Respostas de erro

```json
{
  "momento": "2026-11-01T18:01:30.026",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "O veiculo de id 1 nao pertence ao cliente de id 5",
  "caminho": "/empresa/1/vendas"
}
```

Erros de validação acrescentam a lista de campos, todos de uma vez:

```json
{
  "status": 400,
  "mensagem": "Os dados enviados nao passaram na validacao.",
  "erros": [
    { "campo": "telefones[0].ddd", "mensagem": "O DDD deve conter exatamente 2 digitos" },
    { "campo": "endereco.cidade", "mensagem": "A cidade e obrigatoria" }
  ]
}
```

### Validações e regras

| Campo ou operação | Regra |
|---|---|
| `usuario.nome` · `perfis` | obrigatórios no cadastro; ao menos um perfil |
| `usuario.dataNascimento` | se informada, deve estar no passado |
| `empresa.razaoSocial` | obrigatória no cadastro |
| `documento.tipo` · `dataEmissao` · `numero` | obrigatórios; a emissão não pode estar no futuro; `numero` é único no sistema |
| `telefone.ddd` | 2 dígitos |
| `telefone.numero` | 8 ou 9 dígitos |
| `email.endereco` | obrigatório, em formato de e-mail |
| `endereco.cidade` · `rua` · `numero` | obrigatórios |
| `endereco.codigoPostal` | se informado, 8 dígitos |
| `credencial.nomeUsuario` · `senha` | obrigatórios; `nomeUsuario` é único; senha de 6 a 255 caracteres |
| `credencial.codigo` | obrigatório, positivo e único |
| `veiculo.tipo` · `modelo` · `placa` | obrigatórios; placa única, no formato `ABC1234` ou `ABC1D23` |
| `mercadoria.nome` · `fabricacao` · `quantidade` · `valor` | obrigatórios; fabricação não pode estar no futuro; quantidade e valor não podem ser negativos |
| `mercadoria.validade` | opcional; não pode ser anterior à fabricação |
| `servico.nome` · `valor` | obrigatórios; o valor não pode ser negativo |
| `valor` de mercadoria e serviço | até 8 dígitos inteiros e 2 decimais |
| `venda.identificacao` · `clienteId` · `funcionarioId` | obrigatórios; a identificação é única |
| Cliente da venda | deve ter o perfil `CLIENTE` |
| Funcionário da venda | deve ter o perfil `FUNCIONARIO` e estar vinculado à empresa da venda |
| Veículo da venda | opcional; deve pertencer ao cliente da venda |
| Itens da venda | ao menos uma mercadoria ou um serviço, sem repetição, todos da empresa da venda |
| Fornecedor da mercadoria | deve ter o perfil `FORNECEDOR`; a mercadoria tem um fornecedor por vez |
| Exclusão de veículo, mercadoria ou serviço | recusada se aparecer em alguma venda |
| Exclusão de usuário | recusada se for cliente ou funcionário em alguma venda |
| `dataCadastro` · `cadastro` · `criacao` | definidos pelo servidor, não aceitos na entrada |
| Listas nos corpos | `null` dentro de uma lista é recusado; a lista inteira como `null` vale como vazia, e no `PUT /venda/{id}` vale como não enviada |
| Tipos e perfis | aceitos somente pelo nome, nunca por número |

### Limitações conhecidas

- **Estoque:** a venda não baixa a quantidade da mercadoria. O diagrama não tem
  quantidade por item, então uma venda registra cada mercadoria uma única vez.
- **Veículo da venda:** o `PUT /venda/{id}` troca o veículo, mas não o remove.
- **Segurança:** a credencial é apenas cadastrada, sem login, e a senha é guardada
  em texto puro. O enunciado deixa isso para a "atualização de segurança", etapa
  seguinte à "atualização de base".

---

## Alterações da ATVIII

| O que mudou | Onde |
|---|---|
| Classes do novo modelo de dados e enumerações | `entidades`, `enumeracoes` |
| `Cliente` substituído por `Usuario`, com perfis, e-mails e credenciais | `UsuarioControle`, `UsuarioServico`, `EmailControle`, `CredencialControle` |
| CRUD de empresa e vínculo de usuários | `EmpresaControle`, `EmpresaServico` |
| CRUD de veículo | `VeiculoControle`, `VeiculoServico` |
| CRUD de mercadoria e vínculo com o fornecedor | `MercadoriaControle`, `MercadoriaServico` |
| CRUD de serviço | `ServicoControle`, `ServicoServico` |
| CRUD de venda, com as regras de negócio | `VendaControle`, `VendaServico` |
| Links para os novos recursos | `modelo/AdicionadorLink*` |
| Dados de demonstração das novas entidades | `CargaInicial` |
| Testes de todas as camadas | `src/test` |
