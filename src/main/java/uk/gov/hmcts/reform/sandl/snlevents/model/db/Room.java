package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Room implements Serializable {

    @Id
    @Getter
    @Setter
    UUID id;

    @Getter
    @Setter
    String name;

    @JsonIgnore
    @OneToMany(mappedBy = "room")
    private List<Session> sessionList;

    @EqualsAndHashCode.Exclude
    @Getter
    @ManyToOne(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    })
    private RoomType roomType;

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
        if (roomType != null) {
            roomType.getRooms().add(this);
        }
    }
}
