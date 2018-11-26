package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
public class HearingForListingQueriesTest {

    HearingForListingQueries hearingForListingQueries = new HearingForListingQueries();

    @Test
    public void getOrderBy_replacesPropertyAndDirection() {
        String property = "Prop";
        String direction = "asc";

        String expectedQueryPart = "ORDER BY " + property + " " + direction;

        String orderByQueryPart = hearingForListingQueries.getOrderByQueryPart(property, direction);

        assertThat(orderByQueryPart).isEqualTo(expectedQueryPart);
    }

    @Test
    public void getMainQuery_CombinesSelectMainAndOrderPart() {
        String property = "Prop";
        String direction = "asc";

        String orderByQueryPart = hearingForListingQueries.getMainQuery(property, direction);


        // Check if SELECT, FROM and ORDERBY query parts are separated with spaces after concatenation
        assertThat(orderByQueryPart).contains("is_multisession FROM");
        assertThat(orderByQueryPart).contains("is_deleted = false ORDER BY");
    }
}
