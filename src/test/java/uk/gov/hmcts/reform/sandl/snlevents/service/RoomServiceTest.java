package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.RoomType;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.RoomResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RoomServiceTest {
    @InjectMocks
    RoomService roomService;

    @Mock
    RoomRepository roomRepository;

    @Test
    public void getRooms_returnsRoomsFromRepository() {
        val repositoryRooms = createRooms();
        when(roomRepository.findAll()).thenReturn(repositoryRooms);

        val serviceRooms = roomService.getRoomResponses();
        assertThat(serviceRooms).isEqualTo(createRoomsResponses());
    }

    private List<Room> createRooms() {
        Room r = new Room();
        RoomType rt = new RoomType();
        rt.setCode("Code");
        r.setRoomType(rt);

        return Arrays.asList(r);
    }

    private List<RoomResponse> createRoomsResponses() {
        RoomResponse rr = new RoomResponse();
        rr.setRoomTypeCode("Code");
        return Arrays.asList(rr);

    }
}
