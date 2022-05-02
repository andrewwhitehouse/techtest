package com.andrewwhitehouse.hubpay.wallet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.andrewwhitehouse.hubpay.wallet.WalletController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@AutoConfigureJsonTesters
@WebMvcTest(WalletController.class)
public class WalletControllerTest {

    private static final String WALLET_ID = UUID.randomUUID().toString();

    @MockBean
    private WalletService walletService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<BalanceAdjustmentDTO> jsonBalanceAdjustmentRequest;

    @Autowired
    private JacksonTester<TransactionsDTO> jsonTransactionsResponse;

    @Test
    void shouldAddFunds() throws Exception {
        final int ADDITIONAL_AMOUNT_PENCE = 100 * 100;

        MockHttpServletResponse response = addFunds(WALLET_ID, ADDITIONAL_AMOUNT_PENCE);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(walletService).addFunds(WALLET_ID, ADDITIONAL_AMOUNT_PENCE);
    }

    @Test
    void shouldWithdrawFunds() throws Exception {
        final int WITHDRAWAL_AMOUNT_PENCE = 100 * 100;

        MockHttpServletResponse response = withdrawFunds(WALLET_ID, WITHDRAWAL_AMOUNT_PENCE);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(walletService).withdraw(WALLET_ID, WITHDRAWAL_AMOUNT_PENCE);
    }

    @Test
    void shouldHandleWalletNotFoundOnAdd() throws Exception {
        final int AMOUNT_TO_ADD = 5000;
        doThrow(new ResourceNotFoundException()).when(walletService).addFunds(WALLET_ID, AMOUNT_TO_ADD);

        MockHttpServletResponse response = addFunds(WALLET_ID, AMOUNT_TO_ADD);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldHandleWalletNotFoundOnWithdraw() throws Exception {
        final int AMOUNT_TO_WITHDRAW = 5000;
        doThrow(new ResourceNotFoundException()).when(walletService).withdraw(WALLET_ID, AMOUNT_TO_WITHDRAW);

        MockHttpServletResponse response = withdrawFunds(WALLET_ID, AMOUNT_TO_WITHDRAW);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldValidatePageNumber() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/wallets/{walletId}/transactions", WALLET_ID)
                        .queryParam("page", String.valueOf(0))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo("Minimum page number is 1");
    }

    @Test
    void shouldRetrieveTransactionList() throws Exception {
        final int AMOUNT1 = 1000;
        final int AMOUNT2 = 2000;
        TransactionDTO txn1 = TransactionDTO.builder()
                .amountPence(AMOUNT1)
                .created(OffsetDateTime.now())
                .build();
        TransactionDTO txn2 = TransactionDTO.builder()
                .amountPence(AMOUNT2)
                .created(OffsetDateTime.now())
                .build();
        when(walletService.getTransactions(anyString(), anyInt(), anyInt())).thenReturn(
                TransactionsDTO.builder()
                        .walletId(WALLET_ID)
                        .numberOfElements(2)
                        .totalElements(2)
                        .pageNumber(1)
                        .totalPages(1)
                        .transactions(List.of(txn1, txn2))
                        .build());

        MockHttpServletResponse response = mvc.perform(get("/wallets/{walletId}/transactions", WALLET_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        String content = response.getContentAsString();
        System.err.println(content);
        TransactionsDTO returnedTransactionsDTO = jsonTransactionsResponse.parseObject(content);
        assertThat(returnedTransactionsDTO.getTotalElements()).isEqualTo(2);
        assertThat(returnedTransactionsDTO.getPageNumber()).isEqualTo(1);
        assertThat(returnedTransactionsDTO.getWalletId()).isEqualTo(WALLET_ID);

        List<TransactionDTO> returnedTransactionList = returnedTransactionsDTO.getTransactions();
        assertThat(returnedTransactionsDTO.getTransactions()).hasSize(2);
        assertThat(returnedTransactionList.get(0).getCreated()).isNotNull();
        assertThat(returnedTransactionList.get(1).getCreated()).isNotNull();
        assertThat(returnedTransactionList.get(0).getAmountPence()).isEqualTo(AMOUNT1);
        assertThat(returnedTransactionList.get(1).getAmountPence()).isEqualTo(AMOUNT2);
    }

    @Test
    void shouldProcessPageParameters() throws Exception {

        final int PAGE_SIZE = 14;
        final int PAGE_NUMBER = 23;
        mvc.perform(get("/wallets/{walletId}/transactions", WALLET_ID)
                        .queryParam("page", String.valueOf(PAGE_NUMBER))
                        .queryParam("size", String.valueOf(PAGE_SIZE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        verify(walletService, times(1)).getTransactions(WALLET_ID, PAGE_NUMBER, PAGE_SIZE);
    }

    @Test
    void shouldDefaultPageParameters() throws Exception {

        mvc.perform(get("/wallets/{walletId}/transactions", WALLET_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        verify(walletService, times(1)).getTransactions(WALLET_ID, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }

    private MockHttpServletResponse addFunds(String walletId, int amount) throws Exception {
        BalanceAdjustmentDTO additionDTO = new BalanceAdjustmentDTO(amount);
        return mvc.perform(
                        post("/wallets/{walletId}/add-funds", walletId).contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBalanceAdjustmentRequest.write(additionDTO).getJson()))
                .andReturn().getResponse();
    }

    private MockHttpServletResponse withdrawFunds(String walletId, int amount) throws Exception {
        BalanceAdjustmentDTO additionDTO = new BalanceAdjustmentDTO(amount);
        return mvc.perform(
                        post("/wallets/{walletId}/withdraw-funds", walletId).contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBalanceAdjustmentRequest.write(additionDTO).getJson()))
                .andReturn().getResponse();
    }
}
