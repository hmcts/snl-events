package uk.gov.hmcts.reform.sandl.snlevents;

import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.testcategories.IntegrationTest;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.builders.HearingPartBuilder;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.builders.SessionBuilder;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.builders.UpsertSessionBuilder;

@RunWith(SpringRunner.class)
@Category(IntegrationTest.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = Application.class)
@Import(TestConfiguration.class)
public abstract class BaseIntegrationTest {

    @Autowired
    public SessionBuilder sessionBuilder;

    @Autowired
    public UpsertSessionBuilder upsertSessionBuilder;

    @Autowired
    public HearingPartBuilder hearingPartBuilder;

    @Before
    public void before() {
    }
}
