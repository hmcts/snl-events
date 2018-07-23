package uk.gov.hmcts.reform.sandl.snlevents.fakerules;

import org.junit.experimental.categories.Category;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.sandl.snlevents.BaseIntegrationTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@Category(IntegrationTestWithFakeBackend.class)
@AutoConfigureWireMock(port = 8092)
@DirtiesContext
public abstract class BaseIntegrationTestWithFakeRules extends BaseIntegrationTest {
}
