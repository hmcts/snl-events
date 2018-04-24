package uk.gov.hmcts.reform.sandl.snlevents.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sessions")
@EntityListeners(AuditingEntityListener.class)
public class Session implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "CHARACTER VARYING (50)")
    private String id;

    private String judgeId;

    @NotBlank
    @Column(columnDefinition = "timestamptz")
    private OffsetDateTime start;

    @NotBlank
    @Column(columnDefinition = "int4")
    private Duration duration;
}
