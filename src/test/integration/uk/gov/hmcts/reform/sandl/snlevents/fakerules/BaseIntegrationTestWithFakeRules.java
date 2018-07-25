package uk.gov.hmcts.reform.sandl.snlevents.fakerules;

import org.junit.experimental.categories.Category;

import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;

import uk.gov.hmcts.reform.sandl.snlevents.BaseIntegrationTest;
import uk.gov.hmcts.reform.sandl.snlevents.testcategories.IntegrationTestWithFakeRules;

@AutoConfigureWireMock(port = 8191)
@Category(IntegrationTestWithFakeRules.class)
@DirtiesContext
public abstract class BaseIntegrationTestWithFakeRules extends BaseIntegrationTest {
}
