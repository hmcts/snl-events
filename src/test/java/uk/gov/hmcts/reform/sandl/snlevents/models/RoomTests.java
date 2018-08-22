package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.common.ReferenceData;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.RoomType;

import static org.assertj.core.api.Assertions.assertThat;
@RunWith(SpringRunner.class)

public class RoomTests extends ReferenceData {
    Room room = new Room();

    @Test
    public void addRoomType_shouldSetCorrespondentRelationInRoomType() {
        RoomType roomType = new RoomType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);

        room.setRoomType(roomType);

        assertThat(room.getRoomType()).isEqualTo(roomType);
    }

    @Test
    public void addRoomType_whenPassNull_shouldNotSetCorrespondentRelationInRoomType() {
        room.setRoomType(null);

        assertThat(room.getRoomType()).isEqualTo(null);
    }
}
