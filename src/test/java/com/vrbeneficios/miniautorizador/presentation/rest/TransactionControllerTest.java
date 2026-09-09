package com.vrbeneficios.miniautorizador.presentation.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class TransactionControllerTest {

        private static final String DEFAULT_PASSWORD = "1234";

        @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }

        @Test
        @DisplayName("Deve criar cartão com sucesso e retornar saldo inicial de 500.00")
        void shouldCreateCardAndVerifyInitialBalance() throws Exception {
                var cardNumber = generateCardNumber();

                createCard(cardNumber, DEFAULT_PASSWORD);

                mockMvc.perform(get("/cartoes/" + cardNumber))
                                .andExpect(status().isOk())
                                .andExpect(content().string("500.00"));
        }

        @Test
        @DisplayName("Deve autorizar transação com sucesso e debitar do saldo")
        void shouldAuthorizeTransactionAndDeductBalance() throws Exception {
                var cardNumber = generateCardNumber();

                createCard(cardNumber, DEFAULT_PASSWORD);
                performTransaction(cardNumber, DEFAULT_PASSWORD, "10.00")
                                .andExpect(status().isCreated())
                                .andExpect(content().string("OK"));

                mockMvc.perform(get("/cartoes/" + cardNumber))
                                .andExpect(status().isOk())
                                .andExpect(content().string("490.00"));
        }

        @Test
        @DisplayName("Deve rejeitar transação quando saldo for insuficiente (422 SALDO_INSUFICIENTE)")
        void shouldRejectTransactionWhenBalanceIsInsufficient() throws Exception {
                var cardNumber = generateCardNumber();

                createCard(cardNumber, DEFAULT_PASSWORD);
                performTransaction(cardNumber, DEFAULT_PASSWORD, "500.00")
                                .andExpect(status().isCreated())
                                .andExpect(content().string("OK"));

                performTransaction(cardNumber, DEFAULT_PASSWORD, "10.00")
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(content().string("SALDO_INSUFICIENTE"));
        }

        @Test
        @DisplayName("Deve rejeitar transação quando a senha for inválida (422 SENHA_INVALIDA)")
        void shouldRejectTransactionWhenPasswordIsInvalid() throws Exception {
                var cardNumber = generateCardNumber();

                createCard(cardNumber, DEFAULT_PASSWORD);
                performTransaction(cardNumber, "9999", "10.00")
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(content().string("SENHA_INVALIDA"));
        }

        @Test
        @DisplayName("Deve rejeitar transação quando o cartão não existir (422 CARTAO_INEXISTENTE)")
        void shouldRejectTransactionWhenCardDoesNotExist() throws Exception {
                performTransaction("9999999999999999", DEFAULT_PASSWORD, "10.00")
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(content().string("CARTAO_INEXISTENTE"));
        }

        private static String generateCardNumber() {
                return "654" + System.currentTimeMillis();
        }

        private void createCard(String cardNumber, String password) throws Exception {
                var createCardJson = """
                                {
                                    "numeroCartao": "%s",
                                    "senha": "%s"
                                }
                                """.formatted(cardNumber, password);

                mockMvc.perform(post("/cartoes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createCardJson))
                                .andExpect(status().isCreated());
        }

        private ResultActions performTransaction(String cardNumber, String password, String amount) throws Exception {
                var txJson = """
                                {
                                    "numeroCartao": "%s",
                                    "senhaCartao": "%s",
                                    "valor": %s
                                }
                                """.formatted(cardNumber, password, amount);

                return mockMvc.perform(post("/transacoes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(txJson));
        }
}
