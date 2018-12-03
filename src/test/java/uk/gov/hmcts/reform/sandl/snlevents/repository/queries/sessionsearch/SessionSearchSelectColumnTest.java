package uk.gov.hmcts.reform.sandl.snlevents.repository.queries.sessionsearch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
public class SessionSearchSelectColumnTest {

    @Test(expected = SnlEventsException.class)
    public void fromString_throwsExceptionOnIncorrectValue() {
        SearchSessionSelectColumn.fromString("NON EXISTANT ENUM KEY +++_)__)");
    }

    @Test()
    public void fromString_returnsValueOnCorrectValue() {
        String columnName = SearchSessionSelectColumn.SESSION_TYPE_CODE.getColumnName();
        SearchSessionSelectColumn expectedColumn = SearchSessionSelectColumn.SESSION_TYPE_CODE;

        SearchSessionSelectColumn actualColumn = SearchSessionSelectColumn.fromString(columnName);

        assertThat(actualColumn).isEqualTo(expectedColumn);
    }
}
