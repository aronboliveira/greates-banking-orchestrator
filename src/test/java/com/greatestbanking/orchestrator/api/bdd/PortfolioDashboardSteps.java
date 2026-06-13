package com.greatestbanking.orchestrator.api.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class PortfolioDashboardSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private MvcResult lastResult;
    private Long accountId;

    @Given("I am logged in as {string} with password {string}")
    public void iAmLoggedInAs(String username, String password) throws Exception {
        String body = """
            {"username":"%s","password":"%s"}
            """.formatted(username, password);

        lastResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        JsonNode json = jsonBody();
        token = json.get("access_token").asText();
    }

    @When("I create an account with document number {string}")
    public void iCreateAnAccountWithDocumentNumber(String documentNumber) throws Exception {
        String body = """
            {"document_number":"%s"}
            """.formatted(documentNumber);

        lastResult = mockMvc.perform(post("/accounts")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        if (lastResult.getResponse().getStatus() == 201) {
            accountId = jsonBody().get("account_id").asLong();
        }
    }

    @When("I create a transaction for that account with operation type {int} and amount {bigdecimal}")
    public void iCreateATransactionForThatAccountWithOperationTypeAndAmount(int operationTypeId, BigDecimal amount)
            throws Exception {
        String body = """
            {"account_id":%d,"operation_type_id":%d,"amount":%s}
            """.formatted(accountId, operationTypeId, amount);

        lastResult = mockMvc.perform(post("/transactions")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
    }

    @When("I update my profile to display name {string} with avatar {string}")
    public void iUpdateMyProfileToDisplayNameWithAvatar(String displayName, String avatarId) throws Exception {
        String body = """
            {"display_name":"%s","email":"user@example.test","avatar_id":"%s","notifications_enabled":false}
            """.formatted(displayName, avatarId);

        lastResult = mockMvc.perform(patch("/users/me/profile")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
    }

    @When("I request the profile avatar catalog")
    public void iRequestTheProfileAvatarCatalog() throws Exception {
        lastResult = mockMvc.perform(get("/profile-avatars")
                .header(HttpHeaders.AUTHORIZATION, bearer()))
            .andReturn();
    }

    @When("I request my current profile without a token")
    public void iRequestMyCurrentProfileWithoutAToken() throws Exception {
        token = null;
        lastResult = mockMvc.perform(get("/auth/me")).andReturn();
    }

    @Then("the last response status should be {int}")
    public void theLastResponseStatusShouldBe(int status) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(status);
    }

    @Then("the transaction amount should be {bigdecimal}")
    public void theTransactionAmountShouldBe(BigDecimal expectedAmount) throws Exception {
        assertThat(jsonBody().get("amount").decimalValue()).isEqualByComparingTo(expectedAmount);
    }

    @Then("my profile display name should be {string}")
    public void myProfileDisplayNameShouldBe(String expectedDisplayName) throws Exception {
        assertThat(jsonBody().get("display_name").asText()).isEqualTo(expectedDisplayName);
    }

    @Then("the avatar catalog should include {string}")
    public void theAvatarCatalogShouldInclude(String avatarId) throws Exception {
        JsonNode json = jsonBody();
        assertThat(json).anyMatch(node -> avatarId.equals(node.get("id").asText()));
    }

    private JsonNode jsonBody() throws Exception {
        return objectMapper.readTree(lastResult.getResponse().getContentAsString());
    }

    private String bearer() {
        return "Bearer " + token;
    }
}
