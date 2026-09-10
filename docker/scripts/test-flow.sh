#!/usr/bin/env bash

# ==============================================================================
# Script de Teste E2E - Mini-Autorizador
# Executa os passos definidos na especificação do projeto (INSTRUCTION.md)
# ==============================================================================

BASE_URL="http://localhost:8080"
CARD_NUMBER="654$(date +%s%N | cut -b1-13)"
CARD_PASS="1234"

# Cores para formatação de saída
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

echo -e "${CYAN}${BOLD}======================================================================${NC}"
echo -e "${CYAN}${BOLD}           MINI-AUTORIZADOR - SUÍTE DE TESTES E2E                     ${NC}"
echo -e "${CYAN}${BOLD}======================================================================${NC}"
echo -e "Target URL: ${BASE_URL}"
echo -e "Número do Cartão de Teste: ${CARD_NUMBER}"
echo -e "Senha: ${CARD_PASS}"
echo -e "----------------------------------------------------------------------"

# 0. Verificar se a aplicação está respondendo
echo -e "\n${YELLOW}[0/5] Verificando disponibilidade da aplicação...${NC}"
HEALTH_CHECK=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/cartoes/${CARD_NUMBER}")
if [ "$HEALTH_CHECK" -eq 000 ]; then
  echo -e "${RED}❌ Erro: Aplicação não está rodando em ${BASE_URL}.${NC}"
  echo -e "${YELLOW}Por favor, inicie a aplicação com 'mvn spring-boot:run' ou via Docker Compose antes de executar este script.${NC}"
  exit 1
fi
echo -e "${GREEN}✔ Aplicação ativa e respondendo!${NC}"

# ==============================================================================
# PASSOS DO TESTE
# ==============================================================================

# 1. Criação de um cartão
echo -e "\n${YELLOW}[1/5] Passando pelo teste: Criação de um cartão...${NC}"
CREATE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/cartoes" \
  -H "Content-Type: application/json" \
  -d "{\"numeroCartao\": \"${CARD_NUMBER}\", \"senha\": \"${CARD_PASS}\"}")

CREATE_BODY=$(echo "$CREATE_RESPONSE" | sed -e 's/HTTP_STATUS:.*//g')
CREATE_STATUS=$(echo "$CREATE_RESPONSE" | tr -d '\n' | sed -e 's/.*HTTP_STATUS://')

if [ "$CREATE_STATUS" -eq 201 ]; then
  echo -e "${GREEN}✔ Cartão criado com sucesso (HTTP 201). Resposta:${NC} ${CREATE_BODY}"
else
  echo -e "${RED}❌ Falha na criação do cartão (HTTP ${CREATE_STATUS}). Resposta:${NC} ${CREATE_BODY}"
  exit 1
fi

# 2. Verificação do saldo do cartão recém-criado
echo -e "\n${YELLOW}[2/5] Passando pelo teste: Verificação do saldo do cartão recém-criado...${NC}"
BALANCE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "${BASE_URL}/cartoes/${CARD_NUMBER}")
BALANCE_BODY=$(echo "$BALANCE_RESPONSE" | sed -e 's/HTTP_STATUS:.*//g')
BALANCE_STATUS=$(echo "$BALANCE_RESPONSE" | tr -d '\n' | sed -e 's/.*HTTP_STATUS://')

if [ "$BALANCE_STATUS" -eq 200 ]; then
  echo -e "${GREEN}✔ Saldo obtido com sucesso (HTTP 200). Saldo inicial:${NC} R$ ${BALANCE_BODY}"
else
  echo -e "${RED}❌ Falha ao obter saldo (HTTP ${BALANCE_STATUS}).${NC}"
  exit 1
fi

# 3. Realização de diversas transações, verificando-se o saldo em seguida, até que retorne saldo insuficiente
echo -e "\n${YELLOW}[3/5] Passando pelo teste: Diversas transações até saldo insuficiente...${NC}"

# Transação 1: R$ 200,00
echo -e "  -> Executando Transação 1 (R$ 200,00)..."
TX1_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/transacoes" \
  -H "Content-Type: application/json" \
  -d "{\"numeroCartao\": \"${CARD_NUMBER}\", \"senhaCartao\": \"${CARD_PASS}\", \"valor\": 200.00}")
TX1_BODY=$(echo "$TX1_RESPONSE" | sed -e 's/HTTP_STATUS:.*//g')
TX1_STATUS=$(echo "$TX1_RESPONSE" | tr -d '\n' | sed -e 's/.*HTTP_STATUS://')
echo -e "     Status: HTTP ${TX1_STATUS} | Body: ${TX1_BODY}"

CURR_BAL1=$(curl -s "${BASE_URL}/cartoes/${CARD_NUMBER}")
echo -e "     Saldo atualizado: R$ ${CURR_BAL1}"

# Transação 2: R$ 200,00
echo -e "  -> Executando Transação 2 (R$ 200,00)..."
TX2_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/transacoes" \
  -H "Content-Type: application/json" \
  -d "{\"numeroCartao\": \"${CARD_NUMBER}\", \"senhaCartao\": \"${CARD_PASS}\", \"valor\": 200.00}")
TX2_BODY=$(echo "$TX2_RESPONSE" | sed -e 's/HTTP_STATUS:.*//g')
TX2_STATUS=$(echo "$TX2_RESPONSE" | tr -d '\n' | sed -e 's/.*HTTP_STATUS://')
echo -e "     Status: HTTP ${TX2_STATUS} | Body: ${TX2_BODY}"

CURR_BAL2=$(curl -s "${BASE_URL}/cartoes/${CARD_NUMBER}")
echo -e "     Saldo atualizado: R$ ${CURR_BAL2}"

# Transação 3: R$ 150,00 (Devendo estourar o saldo restante de R$ 100,00)
echo -e "  -> Executando Transação 3 (R$ 150,00 - Excedendo saldo)..."
TX3_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/transacoes" \
  -H "Content-Type: application/json" \
  -d "{\"numeroCartao\": \"${CARD_NUMBER}\", \"senhaCartao\": \"${CARD_PASS}\", \"valor\": 150.00}")
TX3_BODY=$(echo "$TX3_RESPONSE" | sed -e 's/HTTP_STATUS:.*//g')
TX3_STATUS=$(echo "$TX3_RESPONSE" | tr -d '\n' | sed -e 's/.*HTTP_STATUS://')
echo -e "     Status: HTTP ${TX3_STATUS} | Body: ${TX3_BODY}"

CURR_BAL3=$(curl -s "${BASE_URL}/cartoes/${CARD_NUMBER}")
echo -e "     Saldo mantido: R$ ${CURR_BAL3}"

if [ "$TX3_STATUS" -eq 422 ] && [ "$TX3_BODY" == "SALDO_INSUFICIENTE" ]; then
  echo -e "${GREEN}✔ Transação barrada corretamente por SALDO_INSUFICIENTE (HTTP 422)!${NC}"
else
  echo -e "${RED}❌ Falha: Esperava HTTP 422 SALDO_INSUFICIENTE, obteve HTTP ${TX3_STATUS} (${TX3_BODY})${NC}"
  exit 1
fi

# 4. Realização de uma transação com senha inválida
echo -e "\n${YELLOW}[4/5] Passando pelo teste: Transação com senha inválida...${NC}"
PASS_FAIL_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/transacoes" \
  -H "Content-Type: application/json" \
  -d "{\"numeroCartao\": \"${CARD_NUMBER}\", \"senhaCartao\": \"9999\", \"valor\": 10.00}")
PASS_FAIL_BODY=$(echo "$PASS_FAIL_RESPONSE" | sed -e 's/HTTP_STATUS:.*//g')
PASS_FAIL_STATUS=$(echo "$PASS_FAIL_RESPONSE" | tr -d '\n' | sed -e 's/.*HTTP_STATUS://')

if [ "$PASS_FAIL_STATUS" -eq 422 ] && [ "$PASS_FAIL_BODY" == "SENHA_INVALIDA" ]; then
  echo -e "${GREEN}✔ Transação barrada corretamente por SENHA_INVALIDA (HTTP 422)!${NC}"
else
  echo -e "${RED}❌ Falha: Esperava HTTP 422 SENHA_INVALIDA, obteve HTTP ${PASS_FAIL_STATUS} (${PASS_FAIL_BODY})${NC}"
  exit 1
fi

# 5. Realização de uma transação com cartão inexistente
echo -e "\n${YELLOW}[5/5] Passando pelo teste: Transação com cartão inexistente...${NC}"
CARD_FAIL_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/transacoes" \
  -H "Content-Type: application/json" \
  -d "{\"numeroCartao\": \"0000000000000000\", \"senhaCartao\": \"1234\", \"valor\": 10.00}")
CARD_FAIL_BODY=$(echo "$CARD_FAIL_RESPONSE" | sed -e 's/HTTP_STATUS:.*//g')
CARD_FAIL_STATUS=$(echo "$CARD_FAIL_RESPONSE" | tr -d '\n' | sed -e 's/.*HTTP_STATUS://')

if [ "$CARD_FAIL_STATUS" -eq 422 ] && [ "$CARD_FAIL_BODY" == "CARTAO_INEXISTENTE" ]; then
  echo -e "${GREEN}✔ Transação barrada corretamente por CARTAO_INEXISTENTE (HTTP 422)!${NC}"
else
  echo -e "${RED}❌ Falha: Esperava HTTP 422 CARTAO_INEXISTENTE, obteve HTTP ${CARD_FAIL_STATUS} (${CARD_FAIL_BODY})${NC}"
  exit 1
fi

echo -e "\n${CYAN}${BOLD}======================================================================${NC}"
echo -e "${GREEN}${BOLD}      🎉 TODOS OS TESTES DO FLUXO FORAM EXECUTADOS COM SUCESSO!      ${NC}"
echo -e "${CYAN}${BOLD}======================================================================${NC}"
