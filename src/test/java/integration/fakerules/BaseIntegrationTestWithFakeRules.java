package integration.fakerules;

import integration.BaseIntegrationTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@AutoConfigureWireMock(port = 8092)
public abstract class BaseIntegrationTestWithFakeRules extends BaseIntegrationTest {
}
