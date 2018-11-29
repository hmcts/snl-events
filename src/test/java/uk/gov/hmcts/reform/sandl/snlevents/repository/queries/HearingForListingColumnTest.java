package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
public class HearingForListingColumnTest {

    @Test(expected = SnlEventsException.class)
    public void fromString_throwsExceptionOnIncorrectValue() {
        HearingForListingColumn.fromString("NON EXISTANT ENUM KEY +++_)__)");
    }

    @Test()
    public void fromString_returnsValueOnCorrectValue() {
        String columnName = HearingForListingColumn.RESERVED_JUDGE_NAME.getColumnName();
        HearingForListingColumn expectedColumn = HearingForListingColumn.RESERVED_JUDGE_NAME;

        HearingForListingColumn actualColumn = HearingForListingColumn.fromString(columnName);

        assertThat(actualColumn).isEqualTo(expectedColumn);
    }
}
