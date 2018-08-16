package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RoomType implements Serializable {

    @Id
    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String description;

    @OneToMany(mappedBy = "roomType")
    private List<Room> rooms;
}
