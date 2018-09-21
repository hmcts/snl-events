package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    @Id
    private String id; // This is MD5 Hash, NOT UUID! It is to handle the deterministic nature of our rules
    private UUID userTransactionId;
    private String type;
    private String severity;
    private String message;
    private OffsetDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "problem_reference", joinColumns = @JoinColumn(name = "problem_id"))
    private List<ProblemReference> references;
}
