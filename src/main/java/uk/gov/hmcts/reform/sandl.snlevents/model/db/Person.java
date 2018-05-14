package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Person implements Serializable {
    @Id
    @Getter
    @Setter
    UUID id;

    @Getter
    @Setter
    String personType;

    @Getter
    @Setter
    String name;

    @Getter
    @Setter
    String username;

    @JsonIgnore
    @OneToMany(mappedBy = "person")
    private List<Session> sessionList;

    @JsonIgnore
    @OneToMany(mappedBy = "person")
    private List<Availability> availabilityList;
}
