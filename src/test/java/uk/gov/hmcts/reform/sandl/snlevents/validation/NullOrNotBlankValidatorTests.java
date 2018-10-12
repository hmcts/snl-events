package uk.gov.hmcts.reform.sandl.snlevents.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.validation.validators.NullOrNotBlankValidator;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class NullOrNotBlankValidatorTests {

    @Test
    public void whenFieldIsNull_ShouldReturnValid() {
        assertThat(new NullOrNotBlankValidator().isValid(null, null)).isTrue();
    }

    @Test
    public void whenFieldHasSomeCharacters_ShouldReturnValid() {
        assertThat(new NullOrNotBlankValidator().isValid(" Some tricky string ", null)).isTrue();
    }

    @Test
    public void whenFieldIsEmptyString_ShouldReturnInvalid() {
        assertThat(new NullOrNotBlankValidator().isValid("", null)).isFalse();
    }

    @Test
    public void whenFieldContainsWhiteCharacters_ShouldReturnInvalid() {
        assertThat(new NullOrNotBlankValidator().isValid(" ", null)).isFalse();
    }
}
