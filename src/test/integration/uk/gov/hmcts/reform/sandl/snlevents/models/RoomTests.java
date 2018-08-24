package uk.gov.hmcts.reform.sandl.snlevents.models;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.RoomType;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomTypeRepository;

import java.util.UUID;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class RoomTests extends BaseIntegrationModelTest  {
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    RoomTypeRepository roomTypeRepository;

    @Test
    public void addRoomType_shouldSetCorrespondentRelationInRoomType() {
        RoomType roomType = new RoomType(REF_TYPE_CODE, REF_TYPE_DESCRIPTION);
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        room.setId(roomId);
        room.setRoomType(roomType);

        roomRepository.saveAndFlush(room);

        RoomType savedRoomType = roomTypeRepository.findOne(REF_TYPE_CODE);
        Room savedRoom = roomRepository.findOne(roomId);

        assertThat(savedRoomType.getRooms().size()).isEqualTo(1);
        assertThat(savedRoom.getRoomType()).isEqualTo(roomType);
    }
}
