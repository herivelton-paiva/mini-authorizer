# 💳 Mini-Autorizador de Cartões de Benefícios

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3.4](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Build](https://img.shields.io/badge/Build-Passing-success.svg)]()

Um mini-autorizador de cartões de benefícios desenvolvido em **Java 21** e **Spring Boot**, focado em consistência transacional, baixo acoplamento e tratamento de concorrência.

---

## 🏗️ Arquitetura e Decisões de Design

### 1. Banco de Dados Relacional (RDBMS) vs. Não-Relacional (NoSQL)
A escolha por um **Banco de Dados Relacional (MySQL)** em detrimento de uma solução NoSQL/Chave-Valor foi pautada nos seguintes fatores:
* **Garantias ACID Rigorosas**: Em operações financeiras e de autorização de débito em cartão, a consistência dos dados é inegociável (*Atomicidade, Consistência, Isolamento e Durabilidade*).
* **Suporte Nativo a Locks Transacionais**: O modelo relacional fornece suporte maduro a instruções de bloqueio em nível de linha (`SELECT ... FOR UPDATE`), garantindo que alterações no saldo de um cartão ocorram em isolamento estrito sem estado inconsistente intermediário.
* **Integridade dos Dados**: Constraints em nível de schema impedem duplicações e garantem integridade referencial.

---

### 2. Clean Architecture

Foi adotada a **Clean Architecture** (Arquitetura Limpa) dividindo a aplicação em camadas concêntricas bem delimitadas:

```
src/main/java/com/vrbeneficios/miniautorizador/
├── domain/            # Camada Core: Entidades, Exceções e Regras de Negócio (Strategy)
├── application/       # Casos de Uso, DTOs e Interfaces de Repositório
├── infrastructure/    # Persistência JPA, Entidades de Banco e Mapeamentos
└── presentation/      # Controllers REST, DTOs de Apresentação e Handlers de Exceção
```

#### Por que Clean Architecture?
A **Clean Architecture** foi escolhida por priorizar a clareza e centralidade do **Domínio e dos Casos de Uso**. Ela mantém as regras de autorização 100% puras, desprovidas de qualquer anotação ou acoplamento com frameworks externos (como Spring ou JPA).

---

### 3. Design Pattern: Strategy para Validação de Regras

As regras de autorização de transação foram modeladas utilizando o **Design Pattern Strategy**.

* **Interface Comum**: `AuthorizationRule`
* **Estratégias Concretas**:
  * `CardExistenceRule`: Valida se o cartão existe na base (`CARTAO_INEXISTENTE`).
  * `CardPasswordRule`: Valida se a senha informada corresponde à senha do cartão (`SENHA_INVALIDA`).
  * `CardBalanceRule`: Valida se o cartão possui saldo suficiente para o valor da transação (`SALDO_INSUFICIENTE`).
* **Contexto de Execução**: `AuthorizationEngine` recebe a lista de estratégias (ordenadas via anotação `@Order`) e as executa iterativamente.

**Justificativa**: Esta abordagem encapsula a lógica de cada regra individualmente, elimina estruturas condicionais procedurais (como blocos `if/else` gigantes) e facilita a adição de novas regras futuras de maneira totalmente desacoplada.

---

### 4. Tratamento de Concorrência: Pessimistic Locking vs. Optimistic Locking

#### Por que utilizar Pessimistic Locking no projeto?
No projeto foi adotado o **Pessimistic Locking** (`SELECT ... FOR UPDATE` via `findByCardNumberWithLock`) durante a busca do cartão no banco de dados.

* **Isolamento por Cartão**: O bloqueio ocorre estritamente na linha do cartão consultado. Se 1.000 clientes realizarem transações em cartões diferentes simultaneamente, a execução ocorre em 100% de paralelismo sem bloqueios entre cartões distintos.
* **Garantia no Desafio de Concorrência**: Quando duas transações de R$ 10,00 chegam **exatamente ao mesmo milissegundo** para um cartão com saldo de R$ 10,00:
  1. A 1ª requisição adquire o lock na linha da tabela `cards`, lê o saldo (R$ 10,00), autoriza a transação, debita o valor e atualiza o saldo para R$ 0,00 comitando a transação.
  2. A 2ª requisição fica aguardando a liberação do lock. Ao adquiri-lo, lê o saldo atualizado (R$ 0,00), a `CardBalanceRule` é acionada e retorna imediatamente `SALDO_INSUFICIENTE` (HTTP 422) de forma determinística.

#### Pessimistic Lock em Teste vs. Optimistic Lock com Retry em Produção:
* **No ambiente de testes**: O *Pessimistic Locking* garante comportamento 100% síncrono e determinístico para passar no teste de concorrência de duas requisições simultâneas sem falhas de versão não tratadas.
* **Recomendação para Produção (High-Throughput)**: Em um cenário real de altíssimo tráfego (milhares de transações/segundo no mesmo cartão), o *Pessimistic Lock* pode gerar retenção de conexões no pool do banco de dados. Para produção, a melhor abordagem seria o **Optimistic Locking** (`@Version`) combinado com uma estratégia de **Retry com Exponential Backoff**:

1. Duas requisições leem o cartão com a coluna de controle `@Version` igual a `1`.
2. A primeira atualiza a linha incrementando a versão para `2`.
3. A segunda tenta atualizar usando `WHERE version = 1`, o que falha lançando `OptimisticLockException`.
4. O mecanismo de **Retry** intercepta a exceção, busca novamente o cartão com o saldo atualizado (R$ 0,00), reexecuta as regras e retorna a resposta de negócio correta (`SALDO_INSUFICIENTE`), evitando gargalos no banco.

---

### 5. Cobertura de Testes Automatizados

A aplicação conta com uma suíte abrangente de testes unitários e de integração:

* **Testes Unitários**:
  * `AuthorizeTransactionUseCaseTest`: Valida a lógica de negócio do caso de uso de autorização utilizando `@InjectMocks` e `@Mock`.
  * `CreateCardUseCaseTest`: Valida a criação de cartões e o tratamento da exceção `CardAlreadyExistsException`.
  * `GetCardBalanceUseCaseTest`: Valida a consulta de saldo e o retorno quando o cartão não é encontrado.
  * `AuthorizationRulesTest`: Valida cada classe de regra (*Strategy*) individualmente utilizando mocks do Mockito.
  * `AuthorizationEngineTest`: Valida se o motor de execução dispara todas as regras na ordem correta.
* **Testes de Integração**:
  * `TransactionControllerTest`: Executado com `@SpringBootTest` e `MockMvc`. Valida o fluxo completo HTTP real.

---

## 🚀 Como Executar

### Pré-requisitos
* Java 21
* Maven 3.8+
* Docker e Docker Compose

### 1. Executar com Docker Compose
```bash
docker-compose up -d
```

### 2. Executar localmente
```bash
mvn clean spring-boot:run
```

### 3. Executar os Testes Automatizados
```bash
mvn test
```

### 4. Executar o Script de Teste

Na pasta `docker/scripts/` encontra-se o script `test-flow.sh`.

> **Pré-requisito**: Para executar o teste, a **aplicação precisa estar rodando** (`http://localhost:8080`) e o **banco de dados no ar**.

O script executa e valida os seguintes cenários:
* **Criação de um cartão** (`POST /cartoes`)
* **Verificação do saldo do cartão recém-criado** (`GET /cartoes/{numeroCartao}`)
* **Realização de diversas transações, verificando-se o saldo em seguida, até que o sistema retorne informação de saldo insuficiente** (`POST /transacoes`)
* **Realização de uma transação com senha inválida** (`POST /transacoes` com senha incorreta -> `SENHA_INVALIDA`)
* **Realização de uma transação com cartão inexistente** (`POST /transacoes` com cartão inválido -> `CARTAO_INEXISTENTE`)

**Comando para executar:**
```bash
cd docker/scripts
./test-flow.sh
```

---

## 🔮 Próximos Passos & Evolução do Projeto

1. **Controle de Concorrência de Alta Performance (Optimistic Locking + Retry)**:
   * Migração para **Optimistic Locking** (`@Version`) combinado com **Retry automático e Exponential Backoff** (Resilience4j) para evitar contenção de conexões no banco sob cargas de altíssimo tráfego.

2. **Criptografia e Segurança de Dados**:
   * Criptografia/Hashing de senhas dos cartões e mascaramento do número de cartão em logs/respostas de API.

3. **Arquitetura de Cache Distribuído (Redis)**:
   * Camada de cache com **Redis** para rápida validação de existência de cartão e senha, aliviando requisições ao RDBMS.

4. **Observabilidade e Métricas**:
   * Métricas de throughput (TPS) e latência p95/p99 via **Prometheus** / **Grafana**.