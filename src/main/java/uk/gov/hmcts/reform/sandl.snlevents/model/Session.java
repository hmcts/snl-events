package uk.gov.hmcts.reform.sandl.snlevents.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import javax.persistence.ManyToOne;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Session implements Serializable {

    @Id
    private UUID id;

    @ManyToOne
    private Person person;

    @NotNull
    private OffsetDateTime start;

    @NotNull
    private Duration duration;
}
