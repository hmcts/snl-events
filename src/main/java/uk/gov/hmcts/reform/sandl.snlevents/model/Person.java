package uk.gov.hmcts.reform.sandl.snlevents.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person implements Serializable {
    @Id
    UUID id;

    String personType;

    String name;

    @JsonIgnore
    @OneToMany(mappedBy = "person")
    private List<Session> sessionList;
}
