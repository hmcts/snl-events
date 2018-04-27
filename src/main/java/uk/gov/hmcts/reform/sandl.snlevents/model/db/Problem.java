package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    @Id
    private String id;

    private String message;
}
