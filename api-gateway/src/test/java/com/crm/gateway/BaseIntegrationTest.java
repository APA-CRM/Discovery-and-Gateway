package com.crm.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    protected static WireMockServer wireMock;
    protected final ObjectMapper objectMapper = new ObjectMapper();
    @LocalServerPort
    private int localServerPort;

    @BeforeAll
    static void startWiremock() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        configureFor("localhost", wireMock.port());
    }

    @AfterAll
    static void stopWiremock() {
        wireMock.stop();
    }

    @PostConstruct
    public void init() {
        RestAssured.port = localServerPort;
        objectMapper.findAndRegisterModules();
    }

}