package uk.gov.hmcts.reform.sandl.snlevents.models;

import uk.gov.hmcts.reform.sandl.snlevents.common.ReferenceData;
import uk.gov.hmcts.reform.sandl.snlevents.fakerules.BaseIntegrationTestWithFakeRules;

public abstract class BaseIntegrationModelTest extends BaseIntegrationTestWithFakeRules {
    static final String MAIN_TYPE_CODE = ReferenceData.MAIN_TYPE_CODE;
    static final String MAIN_TYPE_DESCRIPTION = ReferenceData.MAIN_TYPE_DESCRIPTION;
    static final String REF_TYPE_CODE = ReferenceData.REF_TYPE_CODE;
    static final String REF_TYPE_DESCRIPTION = ReferenceData.REF_TYPE_DESCRIPTION;
}
