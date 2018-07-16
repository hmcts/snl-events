package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;

import java.util.ArrayList;
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

        val serviceRooms = roomService.getRooms();
        assertThat(serviceRooms).isEqualTo(repositoryRooms);
    }

    private List<Room> createRooms() {
        return new ArrayList<>(Arrays.asList(new Room()));
    }
}
