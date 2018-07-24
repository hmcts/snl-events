package uk.gov.hmcts.reform.sandl.snlevents;

import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.testcategories.IntegrationTest;

@RunWith(SpringRunner.class)
@Category(IntegrationTest.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = Application.class)
public abstract class BaseIntegrationTest {

    @Before
    public void before() {
    }
}
