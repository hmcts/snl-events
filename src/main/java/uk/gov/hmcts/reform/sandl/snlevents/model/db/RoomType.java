package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RoomType implements Serializable {

    public RoomType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Id
    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String description;

    @JsonIgnore
    @Getter
    @OneToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
        }, mappedBy = "roomType")
    private Set<Room> rooms = new HashSet<>();
}
