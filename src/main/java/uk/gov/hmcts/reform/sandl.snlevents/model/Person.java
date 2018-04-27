package uk.gov.hmcts.reform.sandl.snlevents.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Person implements Serializable {
    @Id
    UUID id;

    @Getter
    @Setter
    String personType;

    @Getter
    @Setter
    String name;

    @JsonIgnore
    @OneToMany(mappedBy = "person")
    private List<Session> sessionList;
}
