package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import java.time.OffsetDateTime;

public interface  HistoryAuditable {
    OffsetDateTime getCreatedAt();

    void setCreatedAt(OffsetDateTime createdAt);

    String getCreatedBy();

    void setCreatedBy(String createdBy);

    OffsetDateTime getModifiedAt();

    void setModifiedAt(OffsetDateTime modifiedAt);

    String getModifiedBy();

    void setModifiedBy(String modifiedBy);
}
