package uk.gov.hmcts.reform.sandl.snlevents.fakerules;

import org.junit.experimental.categories.Category;

import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;

import uk.gov.hmcts.reform.sandl.snlevents.BaseIntegrationTest;

// Add @TestPropertySource annotation to inject some values into config (ie Mocked service port)
@AutoConfigureWireMock(port = 8091)
@Category(IntegrationTestWithFakeBackend.class)
@DirtiesContext
public abstract class BaseIntegrationTestWithFakeRules extends BaseIntegrationTest {
}
