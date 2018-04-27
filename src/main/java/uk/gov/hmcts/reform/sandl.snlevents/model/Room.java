package uk.gov.hmcts.reform.sandl.snlevents.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    @Getter
    @Setter
    UUID id;

    @Getter
    @Setter
    String name;

    @JsonIgnore
    @OneToMany(mappedBy = "room")
    @Getter
    @Setter
    List<Session> sessionList;
}
