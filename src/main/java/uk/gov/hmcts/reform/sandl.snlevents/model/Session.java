package uk.gov.hmcts.reform.sandl.snlevents.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;


@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Session implements Serializable {

    @Id
    @Getter
    @Setter
    private UUID id;

    @ManyToOne
    @Getter
    @Setter
    private Person person;

    @NotNull
    @Getter
    @Setter
    private OffsetDateTime start;

    @NotNull
    @Getter
    @Setter
    private Duration duration;

    @ManyToOne
    @Getter
    @Setter
    private Room room;
}
