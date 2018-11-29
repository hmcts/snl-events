package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
public class HearingForListingQueriesTest {

    HearingForListingQueries hearingForListingQueries = new HearingForListingQueries();

    @Test
    public void getMainQuery_CombinesSelectMainAndOrderPart() {
        HearingForListingColumn column = HearingForListingColumn.CASE_NUMBER;
        Sort.Direction dir = Sort.Direction.ASC;

        String query = hearingForListingQueries.getMainQuery(column, dir);

        String expectedOrderByQuery = "ORDER BY " + column.getColumnName() + " " + dir.toString();

        // Check if SELECT, FROM and ORDERBY query parts are separated with spaces after concatenation
        assertThat(query).contains("is_multisession FROM");
        assertThat(query).contains("is_deleted = FALSE ORDER BY");
        assertThat(query).contains(expectedOrderByQuery);
    }
}
