package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("squid:S3437")
public class RoomResponse implements Serializable {
    
    public static RoomResponse fromRoom(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getRoomType().getCode());
    }

    UUID id;

    String name;

    String roomTypeCode;
}


