package com.greatestbanking.orchestrator.api.bdd;

import com.greatestbanking.orchestrator.api.GreatestBankingOrchestratorApiApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = GreatestBankingOrchestratorApiApplication.class)
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {
}
