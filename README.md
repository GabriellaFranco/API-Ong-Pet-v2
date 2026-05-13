Esta API surgiu como uma ideia de projeto para a matéria de Atividade Extensionista: Tecnologia aplicada à inclusão digital,
cujo objetivo era apresentar uma solução que agregasse valor à comunidade por meio da tecnologia. Pensando nisso, desenvolvi
uma solução que permite aos protetores de animais (independentes ou pequenas ONGs) um melhor controle das doações recebidas,
adoções registradas, voluntários ativos, estoque de insumos e finanças — além de conectar animais aos adotantes mais compatíveis
através de um sistema de recomendação inteligente.

Esta versão representa uma **refatoração completa** da versão original (https://github.com/GabriellaFranco/API-Ong-Animais), com correção de bugs críticos, novas funcionalidades e
uma infraestrutura modernizada.

<img width="1252" height="907" alt="image" src="https://github.com/user-attachments/assets/78780968-475c-4dd3-85ae-fbb119897180" />

## Arquitetura

O projeto utiliza a arquitetura em camadas, onde a **Repository Layer** é responsável pelas operações 
de banco de dados, a **Service Layer** concentra a lógica de negócio e a **Controller Layer** atua como 
interface REST. Essa abordagem facilita a execução de testes e a realização de manutenções futuras, pois 
cada camada possui um papel bem definido.

Na refatoração, foi adicionada uma camada de **mensageria assíncrona** com RabbitMQ, desacoplando os 
eventos de domínio (criação de pedidos, mudanças de status, doações) das suas consequências. Isso garante 
que a resposta HTTP seja imediata, enquanto os efeitos colaterais são processados em background.

<br>

| Pacote | Responsabilidade |
|--------|-----------------|
| `config` | Configurações de segurança, JWT, RabbitMQ e S3 |
| `controller` | Endpoints REST |
| `entity` | Entidades JPA |
| `enums` | Enumerações do domínio |
| `exception` | Exceções customizadas e handler global |
| `mapper` | Conversão entre entidades e DTOs |
| `messaging` | Publishers e consumers do RabbitMQ |
| `model` | DTOs e eventos de domínio |
| `repository` | Repositórios Spring Data JPA |
| `service` | Lógica de negócio |

## O que mudou em relação à versão anterior

### 🐛 Bugs corrigidos

<br>

| Bug | Versão anterior | Versão atual |
|-----|----------------|--------------|
| Typo no `@PreAuthorize` | `hasHole('VOLUNTARIO')` — endpoint nunca autorizava ninguém | `hasRole('VOLUNTARIO')` — corrigido |
| Expiração do JWT | `100000 * 60 * 60 ms` ≈ 250 dias | 24 horas configurável via `.env` |
| `@RequestBody` em GETs | Violations HTTP nos filtros | Substituído por `@RequestParam` |
| `Animal.disponivel` nulo | Sem valor padrão — risco de `NullPointerException` | `default true` no banco e na entidade |
| `TipoDoacao` inutilizado | Enum existia mas não era usado | Campo `categoria` adicionado à entidade `Doacao` |
| Ownership no `updateUsuario` | Qualquer usuário autenticado alterava qualquer perfil | Só o próprio usuário ou ADMIN |
| `ddl-auto: update` | Schema gerenciado de forma imprevisível | Substituído por Flyway com migrations versionadas |

<br>

Por exemplo, a expiração do token JWT foi corrigida de um valor que resultava em aproximadamente 250 dias 
para **24 horas configuráveis via variável de ambiente**:

<br>

<img width="1016" height="288" alt="image" src="https://github.com/user-attachments/assets/7798cacc-e32c-40e9-9a96-7afc7ef24f2c" />

## Funcionalidades

### Autenticação via Usuário/Senha e geração de token JWT com Spring Security

A autenticação é baseada em JWT com Spring Security. O token possui expiração de **24 horas** configurável 
via variável de ambiente `JWT_EXPIRATION_MS`, e as propriedades foram extraídas para uma classe 
`JwtProperties` com `@ConfigurationProperties`, eliminando valores hardcoded.

### Proteção de endpoints e métodos com Spring Security

A autorização é baseada em 3 roles distintas — **ADMIN**, **VOLUNTARIO** e **PADRAO** — mantendo o uso de 
`@PreAuthorize` nos métodos de serviço. Na refatoração, foram corrigidos os endpoints públicos no 
`SecurityFilterChain`: animais e listagem pública agora são acessíveis sem autenticação, enquanto operações 
sensíveis continuam protegidas.

<br>

<img width="1336" height="595" alt="image" src="https://github.com/user-attachments/assets/b326f937-bf38-49fd-bf06-254509a97f69" />

<br>

### Auditoria automática em todas as entidades *(novo)*

Foi criada uma classe base abstrata `AuditableEntity` que todas as entidades estendem. Isso garante que todos os 
registros possuam `createdAt` e `updatedAt` preenchidos automaticamente sem necessidade de código manual em cada 
service.

<br>

<img width="511" height="378" alt="image" src="https://github.com/user-attachments/assets/ad81be53-e3ac-4d5f-806d-0f488285782e" />

### Migrações de banco de dados com Flyway *(novo)*

O `ddl-auto: update` foi substituído por `ddl-auto: validate` + Flyway. Todas as mudanças de schema ficam 
versionadas, rastreáveis e reproduzíveis em qualquer ambiente. O Flyway executa os scripts automaticamente 
na inicialização da aplicação, garantindo que o banco esteja sempre na versão correta.

<br>

<img width="365" height="161" alt="image" src="https://github.com/user-attachments/assets/07e566e2-280a-4859-8918-adaebf1cbf76" />

<br>

### DTOs de Request/Response como Records 

Os DTOs foram migrados para **records** (imutáveis por natureza), reduzindo boilerplate e aumentando 
a segurança. As validações continuam via Jakarta Validation com mensagens de erro personalizadas.

<br>

<img width="651" height="682" alt="image" src="https://github.com/user-attachments/assets/14a4e8b8-9602-4124-a3d0-a80e4219b9e7" />

### Mappers para conversão de dados

Juntamente ao padrão DTO, foram mantidas e expandidas as classes de mappers, com o objetivo de facilitar 
a conversão entidade/DTO e vice-versa. Cada domínio possui seu próprio mapper, garantindo que a conversão 
seja isolada e testável.

### Validações personalizadas e transição de status 

As validações de negócio foram expandidas. Além das validações já existentes (animal disponível, pedido 
duplicado, limite de pedidos pendentes), foi implementada uma **validação de transição de status** no 
pedido de adoção, garantindo que o fluxo siga a ordem correta:

SOLICITADA → EM_ANALISE → APROVADA → CONCLUIDA

SOLICITADA → REPROVADA

EM_ANALISE → REPROVADA

Qualquer tentativa de transição inválida retorna 422 Unprocessable Entity com mensagem explicativa.

## Novas Funcionalidades

### 🤝 Matching de Adoção com Score Explicável

O sistema de matching conecta animais disponíveis aos adotantes mais compatíveis, retornando um 
**score de 0 a 100** com os fatores que contribuíram para o cálculo. O algoritmo considera a 
**espécie do animal** em todos os fatores relevantes — por exemplo, felinas pontuam bem em 
apartamentos, enquanto caninas de grande porte em apartamento recebem 0 pontos no fator moradia.

**Fatores avaliados:**

| Fator | Peso | Considera espécie? |
|-------|------|--------------------|
| Espécie preferida | 25 pts | — |
| Porte preferido | 20 pts | Felinos pontuam bem em qualquer porte |
| Tipo de moradia | 20 pts | Apartamento: 18 pts felino, 0 pts canino porte grande |
| Horas em casa | 15 pts | Felinos toleram mais ausência |
| Experiência com animais | 10 pts | Primeiro pet pesa menos para felinos |
| Outros pets | 5 pts | Gatos com outros gatos = risco territorial |
| Faixa de renda | 5 pts | Animais grandes têm custo maior |

**Endpoints:**
- GET /animais/{id}/matching — adotantes compatíveis com um animal (ADMIN/VOLUNTARIO)
- GET /usuarios/me/animais-sugeridos — animais sugeridos para o adotante logado

<br>

<img width="672" height="502" alt="image" src="https://github.com/user-attachments/assets/d4ee6371-e7bb-4ba5-9983-5f2d1b0e5794" />

### Triagem do Adotante com Score de Risco

Antes de criar um pedido de adoção, o adotante preenche um formulário de triagem com informações 
sobre sua moradia, rotina, experiência com animais e renda. O sistema calcula automaticamente um 
**score de risco (0–100)** — quanto maior, menor o risco. Esse score alimenta diretamente o 
algoritmo de matching.

O perfil é criado ou atualizado com um único endpoint: POST /usuarios/me/perfil-adotante

<br>

<img width="335" height="301" alt="image" src="https://github.com/user-attachments/assets/9721b171-568b-4dc5-bfb1-1adcf4c3d772" />

### 📸 Upload e Gestão de Mídias (S3 / MinIO)

Cada animal pode ter uma galeria de fotos e vídeos armazenados no **MinIO** (ambiente local) ou 
**AWS S3** (produção). O tipo do arquivo é detectado pelo conteúdo real via **Apache Tika** — 
não pela extensão — evitando uploads maliciosos.

**Limites e tipos permitidos:**
- Fotos: JPEG, PNG, WebP — máx. 10MB
- Vídeos: MP4 — máx. 50MB

**Funcionalidades da galeria:**
- A primeira foto enviada é automaticamente definida como principal 
- É possível reordenar a galeria via PATCH /animais/{id}/midias/reordenar
- Ao excluir a foto principal, a próxima foto disponível é promovida automaticamente
- A URL pública do arquivo é retornada imediatamente após o upload

<br>

<img width="828" height="190" alt="image" src="https://github.com/user-attachments/assets/ef31b649-6a09-4a6b-90c2-e1f34b89d218" />

<br>

Os arquivos são organizados no bucket por animal (animais/{id}/uuid.jpg), garantindo fácil rastreabilidade no storage:

<br>

<img width="1554" height="784" alt="image" src="https://github.com/user-attachments/assets/c660e793-c058-4b12-9364-5e5511df35d6" />

### 💰 Gestão Financeira e Controle de Estoque

**Gestão financeira:**
- Registro de doações com categorização (DINHEIRO, RAÇÃO, MEDICAMENTO, etc.)
- Registro de despesas vinculáveis a animais específicos
- Resumo financeiro com total de doações, despesas e saldo
- Exportação de relatórios em **CSV** e **PDF**

<br>

<img width="240" height="191" alt="image" src="https://github.com/user-attachments/assets/6d8dcaa3-e46b-4958-aca9-3d55c957ff21" />

<br>

<img width="1132" height="572" alt="image" src="https://github.com/user-attachments/assets/eeefc911-4da1-469d-ba96-0a5a78369ade" />

### Controle de Estoque de Doações

Controla o estoque físico de itens recebidos como doação, com rastreabilidade completa de entradas 
e saídas. O sistema exibe automaticamente a flag estoqueBaixo: true quando a quantidade atual 
fica abaixo do mínimo configurado.

<br>

<img width="458" height="175" alt="image" src="https://github.com/user-attachments/assets/2bc174db-2346-4006-8ca3-d6d869ca82cf" />

<br>

<img width="437" height="246" alt="image" src="https://github.com/user-attachments/assets/f66acab2-fe46-4daa-bb03-bfe47ae51c82" />

<br>
<br>
**Regras de negócio:**
- Saldo calculado sempre via movimentações — nunca atualizado diretamente
- Saídas validam saldo suficiente antes de persistir
- Ajuste de inventário restrito a ADMIN com justificativa obrigatória
- Alerta de estoque baixo via `GET /estoque/itens/alerta`

### Mensageria Assíncrona com RabbitMQ

Todos os eventos de domínio são publicados em filas após o commit da transação, garantindo que 
a resposta HTTP seja imediata enquanto os efeitos colaterais são processados em background. 
A infraestrutura inclui **Dead Letter Exchange**, onde as mensagens que falham após 3 tentativas 
vão para q.dlx.failed para análise posterior.

<br>

| Fila | Evento |
|------|--------|
| q.adocao.criada | Novo pedido de adoção criado |
| q.adocao.status | Status do pedido alterado |
| q.animal.cadastrado | Novo animal cadastrado |
| q.doacao.criada | Nova doação registrada |
| q.dlx.failed | Mensagens que falharam após retries |

<br>

<img width="1242" height="612" alt="image" src="https://github.com/user-attachments/assets/6749fdf7-948f-435b-869c-03932f0b68a4" />

## Testes

O projeto possui testes unitários para todos os services, utilizando **JUnit 5**, **Mockito** e 
**AssertJ**. São 24 testes cobrindo os cenários principais de sucesso e falha de cada domínio.

## Tecnologias Utilizadas

**Core:**
- Java 21
- Spring Boot 3.5.4
- Spring Security + JWT 0.11.5
- Spring Data JPA + Hibernate 6
- PostgreSQL 16
- Flyway (migrações versionadas de banco)
- Lombok

**Adicionadas na refatoração:**
- RabbitMQ 3.13 (mensageria assíncrona com DLX)
- AWS S3 SDK v2 / MinIO (armazenamento de mídias)
- Apache Tika (detecção de MIME type real)
- Apache Commons CSV (exportação CSV)
- iText 8 (geração de PDF)
- Spring Boot Actuator (health check)

**Testes:**
- JUnit 5
- Mockito
- AssertJ
- H2 (banco em memória)

**Documentação:**
- SpringDoc OpenAPI / Swagger UI 2.8.5

**Infraestrutura:**
- Docker + Docker Compose
- Dockerfile multi-stage (Maven + Eclipse Temurin 21 Alpine)

---

## Como Executar

### Pré-requisitos
- Docker Desktop instalado e em execução
- Java 21
- Maven 3.9+

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/ong-pet.git
cd ong-pet
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
POSTGRES_DB=ong-pet
POSTGRES_USER=ongpet
POSTGRES_PASS=ongpet123
RABBITMQ_USER=ongpet
RABBITMQ_PASS=ongpet123
MINIO_ROOT_USER=ongpetadmin
MINIO_ROOT_PASSWORD=ongpet123
MINIO_BUCKET_NAME=ong-pet-midias
JWT_SECRET=4f6e6750657441706949734177657365215468697349734153656372657421313233
JWT_EXPIRATION_MS=86400000
SPRING_PROFILES_ACTIVE=local
```

### 3. Suba a infraestrutura

```bash
docker-compose up -d
```

### 4. Configure as variáveis no IntelliJ

Em **Run → Edit Configurations → Environment Variables** adicione:

SPRING_PROFILES_ACTIVE=local;POSTGRES_DB=ong-pet;POSTGRES_USER=ongpet;POSTGRES_PASS=ongpet123;RABBITMQ_USER=ongpet;RABBITMQ_PASS=ongpet123;MINIO_ROOT_USER=ongpetadmin;MINIO_ROOT_PASSWORD=ongpet123;MINIO_BUCKET_NAME=ong-pet-midias;JWT_SECRET=4f6e6750657441706949734177657365215468697349734153656372657421313233;JWT_EXPIRATION_MS=86400000

### 5. Execute a aplicação

Rode a classe `OngPetApplication` pelo IDE. O Flyway criará todas as tabelas automaticamente.

### 6. Acesse os painéis

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| Swagger UI | http://localhost:8080/swagger-ui/index.html | token JWT |
| RabbitMQ | http://localhost:15672 | ongpet / ongpet123 |
| MinIO | http://localhost:9001 | ongpetadmin / ongpet123 |

### 7. (Opcional) Tudo pelo Docker

```bash
docker-compose up -d --build
```
